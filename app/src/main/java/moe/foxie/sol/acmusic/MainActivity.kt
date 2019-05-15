package moe.foxie.sol.acmusic

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder

import kotlinx.android.synthetic.main.activity_main.*
import android.Manifest

/**
 * the Activity that executes when this app is launched.
 */
class MainActivity : Activity(), MusicPlayerService.ServiceListener {

    private var service: MusicPlayerService? = null
        set(value) {
            value?.serviceListener = this
            field = value
        }

    private val connection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            require(binder is MusicPlayerService.ServiceBinder)
            service = binder.service()
            update()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            check(false) //since service is in-process, this will never be called
        }

    }

    private var permissionsRequested = false
    private lateinit var serviceIntent: Intent

    /**
     * entry point for our app. we start the MusicPlayerService here
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        serviceIntent = Intent(this, MusicPlayerService::class.java).setAction(MUSIC_SERVICE)

        setContentView(R.layout.activity_main)

        playPause.setOnClickListener{
            val playPauseIntent = Intent(this,MusicPlayerService::class.java)
            playPauseIntent.action = MUSIC_SERVICE_PLAY_PAUSE
            startService(playPauseIntent)
        }

        settingsButton.setOnClickListener {
            val settingsIntent = Intent(this,Settings::class.java)
            startActivity(settingsIntent)
        }
    }
    private fun connectToService() {
        if (service == null) this.startForegroundService(serviceIntent)
        this.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>?,
        grantResults: IntArray?
    ) {
        permissionsRequested = true
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()
        if (!permissionsRequested) this.requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 0)
        else connectToService()
    }

    override fun onPause() {
        super.onPause()
        if (service != null) this.unbindService(connection)
    }

    override fun update() {
        this.runOnUiThread {

            service?.let {
                display.text = it.trackDisplayName()
                playPause.setText(it.getPlayerState().uiPlayPauseString())
            }

        }
    }

    override fun serviceExited() {
        service = null
    }
}