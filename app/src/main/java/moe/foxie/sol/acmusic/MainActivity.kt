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


    override fun update(trackID: Pair<Int,ACWeather>?, state: MusicManager.State) {
        this.runOnUiThread {
            playPause.setText(state.uiPlayPauseString())
        }
    }
}