package moe.foxie.sol.acmusic

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.SystemClock
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 * MusicManager plays tracks provided, synced to the hour of day and the weather.
 * Initialize with a Context (for scheduling alarms) and a Map of resource Ints (e.g. those you'd get from R.raw.*)
 * Responsibilities include:
 * * managing a mediaPlayer instance, including playing and pausing and discarding
 * * scheduling alarms and keeping track of changes in the time and weather
 * * when the time/weather changes, the track should be changed and start playing iff the MusicManager is not paused
 */
class MusicManager(private val ctx: Context, private val tracks: Map<TrackID,Int>)
    :BroadcastReceiver(), AlarmManager.OnAlarmListener  {

    private var player: MediaPlayer? = null
    private val alarmManager = ctx.getSystemService(AlarmManager::class.java)

    var listener: TrackChangeListener? = null

    var didChangeBlock: (() -> Unit)? = null

    var updateBlock: ((MusicManager) -> Unit)? = null
        set(value) {
            field = value
            value?.invoke(this)
        }

    var currentlyPlaying: TrackInfo? = null
        private set

    /**
     * represents the state of the MusicManager, for the purpose of other classes
     * to use to determine legitimate operations to perform on a MusicManager instance.
     */
    enum class State {
        PLAYING, PAUSED;

        fun uiPlayPauseString(): Int {
            return when (this) {
                PLAYING -> R.string.pause
                PAUSED -> R.string.play
            }
        }

        fun togglePlayPause(manager: MusicManager) {
            when (this) {
                PLAYING -> manager.pause()
                PAUSED -> manager.play()
            }
        }
    }
    fun getState(): MusicManager.State {
        return if (player?.isPlaying == true) State.PLAYING else State.PAUSED
    }

    /**
     * inits this class and starts playing audio immediately.
     */
    init {
        play()
        scheduleNext()
        ctx.registerReceiver(this,IntentFilter(Intent.ACTION_TIME_CHANGED))
    }

    /**
     * change track according to current hour of the day.
     */
    fun changeTrackID(nextTrack: TrackInfo) {
        val key = nextTrack.trackID

        val previousTrack = this.currentlyPlaying
        this.currentlyPlaying = nextTrack

        val thePlayer = player
        if (previousTrack == null || previousTrack.hour != key.hour) {
            if (thePlayer != null) {
                (MediaFader(thePlayer) { changeTracks(tracks[key] ?: 0) }).fadeout()
            } else {
                changeTracks(tracks[key] ?: 0)
            }
        }
    }

    /**
     * if the current track is paused and the current time/weather hasn't changed,
     * simply resumes playing the existing media player.
     * otherwise, it changes the track to the one appropriate for the current time/weather,
     * but does NOT play the hourly chime.
     */
    fun play() {
        if (player?.isPlaying == false) player?.start()
        updateBlock?.invoke(this)
        didChangeBlock?.invoke()
    }

    /**
     * pauses the currently playing track.
     */
    fun pause() {
        player?.pause()
        didChangeBlock?.invoke()
    }

    fun playPause() {
        this.getState().togglePlayPause(this)
    }

    /**
     * changes the currently playing track.
     * calling this method will replace the existing MediaPlayer with a new instance,
     * as well as notify any MusicManager.TrackChangeListeners that the track did change.
     * the parameter for this function is a resource ID for use with the MediaPlayer(Int) constructor.
     * the only values passed into this method should be those from the Map passed into the constructor of MusicManager,
     * and because that is not enforced in code, this method is private
     */
    private fun changeTracks(trackRes: Int) {
        player?.discard()

        player = MediaPlayer.create(ctx,trackRes)
        player!!.isLooping = true
        player!!.start()

        didChangeBlock?.invoke()
    }

    /**
     * schedules an alarm to fire at the next hour change. current weather should be determined when that alarm fires,
     * not within this method.
     */
    private fun scheduleNext() {
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,  SystemClock.elapsedRealtime() + msToNextHour(), "acmusic", this, null)
    }

    /**
     * respond to the manual changing of the system clock.
     * todo: is ACTION_TIME_CHANGED really only called when the user sets the clock? seems NTP updates may also cause this intent to fire.
     */
    override fun onReceive(ctx: Context?, intent: Intent?) {
        require(intent!!.action.equals(Intent.ACTION_TIME_CHANGED))
        alarmManager.cancel(this)
        onAlarm()
    }

    /**
     * perform actions necessary when the hour (or time) changes,
     * such as changing the track and scheduling the next alarm.
     */
    override fun onAlarm() {
        if (this.getState() == MusicManager.State.PLAYING) {
            updateBlock?.invoke(this)
        }
        scheduleNext()
    }

    /**
     * an interface to be implemented by objects wanting to be notified when the MusicManager changes tracks.
     * an implementing object should be assigned to the MusicManager's listener property.
     */
    interface TrackChangeListener {
        fun trackDidChange()
    }
}

fun MediaPlayer.discard() {
    this.stop()
    this.reset()
    this.release()
}

fun getHour24() = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

fun msToNextHour(): Long {
    return 3600000 - Date().time % 3600000
}

fun trackDisplayName(track: TrackID?): String {
    if (track == null) return ""
    var twelveHour = track.hour % 12
    if (twelveHour == 0) twelveHour = 12
    val ampm = if (track.hour >= 12) "PM" else "AM"
    val weather = when (track.weather) {
        ACWeather.SUNNY -> "Sunny"
        ACWeather.RAINY -> "Rainy"
        ACWeather.SNOWY -> "Snowy"
    }
    val gameName = "New Leaf"

    return "$twelveHour$ampm, $weather ($gameName)"
}

class MediaFader(private val player: MediaPlayer, private val completionBlock: () -> Unit) {

    private val interval: Long = 100 //milliseconds
    private val step = 0.1F
    private var volume = 1.0F
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var handle: ScheduledFuture<*>? = null

    fun fadeout() {
        if (handle == null) {
            handle = executor.scheduleAtFixedRate({ tick() }, 0, interval, TimeUnit.MILLISECONDS)
        }
    }

    private fun tick() {
        volume -= step
        volume = max(volume, 0F)
        this.player.setVolume(volume, volume)
        if (volume == 0F) {
            handle?.cancelGracefully()
            completionBlock()
        }
    }

}

fun ScheduledFuture<*>.cancelGracefully() = this.cancel(false)
