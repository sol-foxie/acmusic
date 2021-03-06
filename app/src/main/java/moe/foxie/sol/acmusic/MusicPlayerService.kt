package moe.foxie.sol.acmusic

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Binder
import android.os.Handler
import android.os.IBinder

/**
 * Service for playing background music.
 */
class MusicPlayerService: Service() {

    private val foregroundServiceNotificationId = 1;

    private val handler = Handler()
    private val serviceIdleKill = { this.stopSelf() }

    private val soundtrackListener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
        if (key == SOUNDTRACK_PREFERENCE)
            switchSoundtracks(prefs.getInt(key, SOUNDTRACK.NEW_LEAF))
    }
    private lateinit var fetcherThread: FetcherThread
    private lateinit var manager: MusicManager
    private lateinit var weather: WeatherManager

    var serviceListener: MusicPlayerService.ServiceListener? = null
    set(value) {
        field = value
        val currentTrack = manager.currentlyPlaying
        if (currentTrack != null) value?.update()
    }

    interface ServiceListener {
        fun update()
        fun serviceExited()
    }

    inner class ServiceBinder: Binder() {
        //this should only be used by callers in the same process!!
        fun service(): MusicPlayerService = this@MusicPlayerService
        var listener: ServiceListener? = null
    }

    private var serviceIsActive = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when(intent?.action) {
            MUSIC_SERVICE -> if (!serviceIsActive) startup()
            MUSIC_SERVICE_PLAY_PAUSE -> playPause()
        }
        return START_NOT_STICKY
    }

    private fun startup() {
        serviceIsActive = true
        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val prefs = getSharedPreferences(PREFERENCES,Context.MODE_PRIVATE)
        prefs.registerOnSharedPreferenceChangeListener(soundtrackListener)

        weather = WeatherManager(true, this, getAPIs(resources))

        manager = MusicManager(this, getTracks(prefs.getInt(SOUNDTRACK_PREFERENCE,SOUNDTRACK.NEW_LEAF)))

        fetcherThread = FetcherThread(weather)
        fetcherThread.setMusicManager(manager)
        fetcherThread.start()

        manager.updateBlock = { fetcherThread.shouldUpdate() }
        manager.didChangeBlock = {
            serviceListener?.update()
            notificationManager.notify(foregroundServiceNotificationId,this.makeForegroundServiceNotification(manager.getState() == MusicManager.State.PLAYING))
        }

        val channel = NotificationChannel(
            SERVICE_NOTIFICATION_CHANNEL,
            getString(R.string.app_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.setShowBadge(false)

        notificationManager.createNotificationChannel(channel)

        this.startForeground(foregroundServiceNotificationId,this.makeForegroundServiceNotification(true))
    }

    private fun makeForegroundServiceNotification(isPlaying: Boolean): Notification {
        val message = if (isPlaying) "Currently playing: ${trackDisplayName()}" else "Currently paused."
        val button = if (isPlaying) "▶️" else "⏸"
        val playPauseIntent = Intent(this,MusicPlayerService::class.java).setAction(MUSIC_SERVICE_PLAY_PAUSE)
        val pPlayPauseIntent = PendingIntent.getService(this,0,playPauseIntent,PendingIntent.FLAG_UPDATE_CURRENT)
        val playPauseAction = Notification.Action.Builder(R.drawable.play,button,pPlayPauseIntent).build()

        return Notification.Builder(this, SERVICE_NOTIFICATION_CHANNEL)
            .setContentTitle(message)
            .setSmallIcon(R.drawable.acleaf)
            .setOnlyAlertOnce(true)
            .addAction(playPauseAction)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceIsActive = false
        serviceListener?.serviceExited()
    }

    private var isBound = false

    override fun onBind(intent: Intent?): IBinder {
        check(serviceIsActive)
        require(intent?.action == MUSIC_SERVICE)
        isBound = true
        return this.ServiceBinder()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        super.onUnbind(intent)
        isBound = false
        if (manager.getState() == MusicManager.State.PAUSED)
            handler.postDelayed(serviceIdleKill,1000*60)
        return true
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        isBound = true
        handler.removeCallbacks(serviceIdleKill)
    }
    fun playPause() {
        check(serviceIsActive)
        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val oldState = manager.getState()
        this.manager.playPause()
        val newState = manager.getState()
        assert(newState != oldState)

        if (newState == MusicManager.State.PAUSED) {
            notificationManager.notify(foregroundServiceNotificationId,makeForegroundServiceNotification(false))
            if (!isBound)
                handler.postDelayed(serviceIdleKill,1000*60)
        } else if (newState == MusicManager.State.PLAYING) {
            notificationManager.notify(foregroundServiceNotificationId, makeForegroundServiceNotification(true))
            handler.removeCallbacks(serviceIdleKill)
        }
    }

    fun trackDisplayName(): String {
        return this.manager.trackDisplayName()
    }

    fun getPlayerState(): MusicManager.State {
        return this.manager.getState()
    }

    private fun getTracks(soundtrack: Int): HashMap<TrackID,Int> {
        return when (soundtrack) {
            SOUNDTRACK.WILD_WORLD -> acwwTracks
            SOUNDTRACK.ANIMAL_CROSSING -> afTracks
            else -> acnlTracks
        }
    }

    private fun switchSoundtracks(soundtrack: Int) {
        val newManager = MusicManager(this,getTracks(soundtrack))
        fetcherThread.setMusicManager(newManager)
        manager.kill()
        newManager.updateBlock = manager.updateBlock
        newManager.didChangeBlock = manager.didChangeBlock
        manager = newManager
    }

    fun isOnline(): Boolean {
        return manager.currentlyPlaying?.forecast?.connection.let {
            it != null && it is WeatherManager.Connectivity.ONLINE
        }
    }

}

fun getAPIs(res: Resources): List<WeatherManager.RemoteAPI> {
    return listOf(DarkSkyApi(res.getString(R.string.darkSkyKey)))
}