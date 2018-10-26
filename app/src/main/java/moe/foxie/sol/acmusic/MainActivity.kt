package moe.foxie.sol.acmusic

import android.app.Activity
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*

/**
 * The Activity that executes when this app is launched.
 */
class MainActivity : Activity(), AdapterView.OnItemSelectedListener, MusicManager.TrackChangeListener {

    // this is null because initializing it before onCreate passes it an unusable Context value for some reason…
    private var manager: MusicManager? = null

    /**
     * entry point for our app. we create an start the MusicManager in here,
     * as well as configure views and event listeners and all that jazz.
     */
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

    /**
     * plays a track as selected from a Spinner view by the user.
     * assumes the listing of tracks in the Spinner are arranged in chronological order,
     * such that 12am is at row 0 and 11pm is at row 23.
     * does not do anything if the track selected is currently being played by the MusicManager.
     * todo: decide whether selecting an item in the Spinner while the MusicManager is paused should make it play
     */
    override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, pos: Int, row: Long) {
        if (row.toInt() != manager!!.getTrackID()) {
            require(adapterView!!.id == jukebox.id)
            manager!!.changeTrackNo(row.toInt())
        }
    }

    //todo: figure out if this even gets called when the view in question is a Spinner
    override fun onNothingSelected(adapterView: AdapterView<*>?) {
    }

    /**
     * if the MusicManager changes tracks while the UI is extant, we should react and select that value in the Spinner.
     */
    override fun trackDidChange() {
        jukebox.setSelection(manager!!.getTrackID())
    }

}