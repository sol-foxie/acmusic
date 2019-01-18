package moe.foxie.sol.acmusic

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.media.session.MediaSession
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

    private lateinit var fetcherThread: FetcherThread
    private lateinit var manager: MusicManager
    private lateinit var weather: WeatherManager

    var serviceListener: MusicPlayerService.ServiceListener? = null
    set(value) {
        field = value
        if (manager.currentlyPlaying != null) value?.update(manager.currentlyPlaying!!,manager.getState())
    }

    interface ServiceListener {
        fun update(track: Pair<Int,WeatherManager.Forecast>, state: MusicManager.State)
        fun serviceExited()
    }

    inner class ServiceBinder: Binder() {
        //this should only be used by callers in the same process!!
        fun service(): MusicPlayerService = this@MusicPlayerService
        var listener: ServiceListener? = null
    }

    var serviceIsActive = false
        private set

    private lateinit var session: MediaSession
    override fun onCreate() {
        super.onCreate()
        session = MediaSession(this,"test")
        session.isActive = true
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            MUSIC_SERVICE -> startup()
            MUSIC_SERVICE_PLAY_PAUSE -> playPause()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startup() {
        serviceIsActive = true

        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
        val str = if (isPlaying) getString(R.string.foreground_notification_playing) else getString(R.string.foreground_notification_paused)
        return Notification.Builder(this, SERVICE_NOTIFICATION_CHANNEL)
            .setContentTitle(str)
            .setSmallIcon(R.drawable.acleaf)
            .setOnlyAlertOnce(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceIsActive = false
        serviceListener?.serviceExited()
    }

    override fun onBind(intent: Intent?): IBinder {
        check(serviceIsActive)
        require(intent?.action == MUSIC_SERVICE)

        weather = WeatherManager(true, this, getAPIs(resources))

        manager = MusicManager(this, acnlTracks)

        fetcherThread = FetcherThread(manager,weather)
        fetcherThread.start()
        manager.updateBlock = { fetcherThread.shouldUpdate() }
        manager.didChangeBlock = { serviceListener?.update(manager.currentlyPlaying!!, manager.getState())}

        return this.ServiceBinder()
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
            handler.postDelayed(serviceIdleKill,1000*60)
        } else if (newState == MusicManager.State.PLAYING) {
            notificationManager.notify(foregroundServiceNotificationId,makeForegroundServiceNotification(true))
            handler.removeCallbacks(serviceIdleKill)
        }
    }

}


const val DOMAIN_PREFIX = "moe.foxie.sol."

const val MUSIC_SERVICE_ONLINE = DOMAIN_PREFIX + "ACTION_MUSIC_SERVICE_ONLINE"
const val MUSIC_SERVICE_OFFLINE = DOMAIN_PREFIX + "ACTION_MUSIC_SERVICE_OFFLINE"
const val MUSIC_SERVICE = DOMAIN_PREFIX + "ACTION_MUSIC_SERVICE"
const val MUSIC_SERVICE_PLAY_PAUSE = DOMAIN_PREFIX + "ACTION_MUSIC_PLAY_PAUSE"

const val SERVICE_NOTIFICATION_CHANNEL = DOMAIN_PREFIX + "SERVICE_NOTIFICATION_CHANNEL"

fun getAPIs(res: Resources): List<WeatherManager.RemoteAPI> {
    return listOf(DarkSkyApi(res.getString(R.string.darkSkyKey)))
}