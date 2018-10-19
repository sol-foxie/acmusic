package moe.foxie.sol.acmusic

import android.content.Context
import android.media.MediaPlayer
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import java.util.*

class MusicManager(private val ctx: Context, private val spinner: Spinner, private val tracks: Map<Int,Int>)
    :AdapterView.OnItemSelectedListener, MediaPlayer.OnCompletionListener  {

    private var player: MediaPlayer? = null

    init {
        update()
    }

    private fun update() {
        changeTracks(tracks[getHour24()] ?: 0)
        spinner.setSelection(getHour24())
    }

    private fun changeTracks(trackNo: Int) {
        player?.discard()

        player = MediaPlayer.create(ctx,trackNo)
        player!!.setOnCompletionListener(this)
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
        update()
    }
}

fun MediaPlayer.discard() {
    this.stop()
    this.reset()
    this.release()
}

fun getHour24() = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)