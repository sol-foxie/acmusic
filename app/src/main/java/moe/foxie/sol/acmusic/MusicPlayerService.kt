package moe.foxie.sol.acmusic

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder

/**
 * Service for playing background music.
 */
class MusicPlayerService: Service() {

    lateinit var manager: MusicManager
        private set
    lateinit var weather: WeatherManager
        private set

    var serviceListener: MusicPlayerService.ServiceListener? = null
    set(value) {
        field = value
        value?.update(manager.getTrackID(),manager.getState())
    }

    interface ServiceListener {
        fun update(trackID: Int, state: MusicManager.State)
    }

    inner class ServiceBinder: Binder() {
        //this should only be used by callers in the same process!!
        fun service(): MusicPlayerService = this@MusicPlayerService
        var listener: ServiceListener? = null
    }

    var firstRun = true

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (firstRun) {
            val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                SERVICE_NOTIFICATION_CHANNEL,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
            val notification = Notification.Builder(this, SERVICE_NOTIFICATION_CHANNEL)
                .setContentTitle("test")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
            this.startForeground(1, notification)
            firstRun = false
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder {
        require(intent != null)
        require(intent.action == MUSIC_SERVICE_ONLINE || intent.action == MUSIC_SERVICE_OFFLINE)

        //weather = WeatherManager(intent.action == MUSIC_SERVICE_ONLINE, this, getAPIs())
        manager = MusicManager(this, acnlTracks)
        manager.didChangeBlock = {
            serviceListener?.update(manager.getTrackID(),manager.getState())
        }

        return this.ServiceBinder()
    }

}


const val DOMAIN_PREFIX = "moe.foxie.sol."

const val MUSIC_SERVICE_ONLINE = DOMAIN_PREFIX + "ACTION_MUSIC_SERVICE_ONLINE"
const val MUSIC_SERVICE_OFFLINE = DOMAIN_PREFIX + "ACTION_MUSIC_SERVICE_OFFLINE"

const val SERVICE_NOTIFICATION_CHANNEL = DOMAIN_PREFIX + "SERVICE_NOTIFICATION_CHANNEL"

fun getAPIs(): List<WeatherManager.RemoteAPI> {
    TODO("not implemented")
}