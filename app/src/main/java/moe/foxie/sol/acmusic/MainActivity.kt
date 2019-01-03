package moe.foxie.sol.acmusic

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.AdapterView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import android.Manifest

/**
 * the Activity that executes when this app is launched.
 */
class MainActivity : Activity(), AdapterView.OnItemSelectedListener, MusicPlayerService.ServiceListener {

    private var service: MusicPlayerService? = null
        set(value) {
            value?.serviceListener = this
            field = value
        }

    private val connection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            require(binder is MusicPlayerService.ServiceBinder)
            service = binder.service()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            check(false) //since service is in-process, this will never be called
        }

    }

    private lateinit var onlineIntent: Intent

    /**
     * entry point for our app. we start the MusicPlayerService here
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),0)

        onlineIntent = Intent(this, MusicPlayerService::class.java).setAction(MUSIC_SERVICE_ONLINE)

        this.startForegroundService(onlineIntent)
        setContentView(R.layout.activity_main)

        jukebox.onItemSelectedListener = this

        playPause.setOnClickListener{
            service?.manager?.playPause()
        }
    }

    override fun onResume() {
        super.onResume()
        this.bindService(onlineIntent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onPause() {
        super.onPause()
        this.unbindService(connection)
    }


    override fun update(trackID: Int, state: MusicManager.State) {
        jukebox.setSelection(trackID)
        playPause.setText(state.uiPlayPauseString())
    }

    /**
     * a Spinner fires a selection when loaded, even if the user didn't interact with it.
     * it's unfortunate, but we need to suppress this and this kludge seems like the best way.
     */
    private var initialSelection = true
    /**
     * plays a track as selected from a Spinner view by the user.
     * assumes the listing of tracks in the Spinner are arranged in chronological order,
     * such that 12am is at row 0 and 11pm is at row 23.
     * does not do anything if the track selected is currently being played by the MusicManager.
     * todo: decide whether selecting an item in the Spinner while the MusicManager is paused should make it play
     */
    override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, pos: Int, row: Long) {
        require(adapterView!!.id == jukebox.id)
        if (!initialSelection) service?.manager?.changeTrackNo(row.toInt())
        initialSelection = false
    }

    //todo: figure out if this even gets called when the view in question is a Spinner
    override fun onNothingSelected(adapterView: AdapterView<*>?) {
    }

}