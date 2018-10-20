package moe.foxie.sol.acmusic

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.SystemClock
import java.util.*


class MusicManager(private val ctx: Context, private val tracks: Map<Int,Int>)
    :BroadcastReceiver(), AlarmManager.OnAlarmListener  {

    private var player: MediaPlayer? = null
    private val alarmManager = ctx.getSystemService(AlarmManager::class.java)

    var listener: TrackChangeListener? = null

    private var trackNo: Int = -1
    fun getTrackID(): Int = trackNo

    init {
        onAlarm()
        ctx.registerReceiver(this,IntentFilter(Intent.ACTION_TIME_CHANGED))
    }

    fun changeTrackNo(trackNo: Int) {
        this.trackNo = trackNo
        changeTracks(tracks[trackNo] ?: 0)
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
        alarmManager.cancel(this)
        scheduleNext()
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

