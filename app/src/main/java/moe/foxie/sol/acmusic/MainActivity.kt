package moe.foxie.sol.acmusic

import android.app.Activity
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity(), AdapterView.OnItemSelectedListener, MusicManager.TrackChangeListener {

    private var manager: MusicManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        manager = MusicManager(this, acnlTracks)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        jukebox.onItemSelectedListener = this

        playPause.setOnClickListener{
            manager!!.getState().togglePlayPause(manager!!)
            playPause.setText(manager!!.getState().uiPlayPauseString())
        }
    }

    override fun onResume() {
        super.onResume()
        jukebox.setSelection(manager!!.getTrackID())
        playPause.setText(manager!!.getState().uiPlayPauseString())
    }

    override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, pos: Int, row: Long) {
        if (row.toInt() != manager!!.getTrackID()) {
            require(adapterView!!.id == jukebox.id)
            manager!!.changeTrackNo(row.toInt())
        }
    }

    override fun onNothingSelected(adapterView: AdapterView<*>?) {
    }

    override fun trackDidChange() {
        jukebox.setSelection(manager!!.getTrackID())
    }

}