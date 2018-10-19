package moe.foxie.sol.acmusic

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import java.util.*


class MusicManager(private val ctx: Context, private val spinner: Spinner, private val tracks: Map<Int,Int>)
    :AdapterView.OnItemSelectedListener, MediaPlayer.OnCompletionListener, AlarmManager.OnAlarmListener  {

    private var player: MediaPlayer? = null
    private val alarmManager = ctx.getSystemService(AlarmManager::class.java)

    init {
        onAlarm()
    }

    private fun scheduleNext() {
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,  System.currentTimeMillis() + msToNextHour(), "acmusic", this, null)
    }

    private fun update() {
        changeTracks(tracks[getHour24()] ?: 0)
        spinner.setSelection(getHour24())
    }

    private fun changeTracks(trackNo: Int) {
        player?.discard()

        player = MediaPlayer.create(ctx,trackNo)
        player!!.isLooping = true
        player!!.start()
    }

    override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, pos: Int, row: Long) {
        require(adapterView!!.id == spinner.id)
        changeTracks(tracks[row.toInt()] ?: 0)
    }

    override fun onNothingSelected(adapterView: AdapterView<*>?) {
        player?.stop()
    }

    override fun onCompletion(player: MediaPlayer?) {
    }

    override fun onAlarm() {
        update()
        scheduleNext()
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