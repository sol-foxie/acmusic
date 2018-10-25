package moe.foxie.sol.acmusic

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.SystemClock
import java.util.*


class MusicManager(private val ctx: Context, private val tracks: Map<Pair<Int,Weather>,Int>)
    :BroadcastReceiver(), AlarmManager.OnAlarmListener  {

    private var player: MediaPlayer? = null
    private val alarmManager = ctx.getSystemService(AlarmManager::class.java)

    var listener: TrackChangeListener? = null

    private var trackNo: Int = -1
    fun getTrackID(): Int = trackNo

    init {
        play()
        scheduleNext()
        ctx.registerReceiver(this,IntentFilter(Intent.ACTION_TIME_CHANGED))
    }

    fun changeTrackNo(trackNo: Int) {
        this.trackNo = trackNo
        changeTracks(tracks[Pair(trackNo,SUNNY)] ?: 0)
    }

    /**
     * if the current track is paused and the current time/weather hasn't changed,
     * simply resumes playing the existing media player.
     * otherwise, it changes the track to the one appropriate for the current time/weather,
     * but does NOT play the hourly chime.
     */
    fun play() {
        //placeholder implementation that assumes weather is unchanging and always sunny
        //at time of writing, hourly chime is unimplemented so we'll ignore that for now too
        if ((player?.isPlaying == false) && trackNo == getHour24()) {
            player?.start()
        }
        else if (trackNo != getHour24()) {
            changeTrackNo(getHour24())
        }
    }

    /**
     * pauses the currently playing track.
     */
    fun pause() {
        player?.pause()
    }

    private fun changeTracks(trackRes: Int) {
        player?.discard()

        player = MediaPlayer.create(ctx,trackRes)
        player!!.isLooping = true
        player!!.start()

        listener?.trackDidChange()
    }

    private fun scheduleNext() {
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,  SystemClock.elapsedRealtime() + msToNextHour(), "acmusic", this, null)
    }

    override fun onReceive(ctx: Context?, intent: Intent?) {
        require(intent!!.action.equals(Intent.ACTION_TIME_CHANGED))
        alarmManager.cancel(this)
        onAlarm()
    }

    override fun onAlarm() {
        changeTrackNo(getHour24())
        scheduleNext()
    }

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

