package moe.foxie.sol.acmusic

import android.os.Bundle
import android.app.Activity
import android.content.res.Resources

import kotlinx.android.synthetic.main.activity_debug.*

class Debug : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)
        refreshButton.setOnClickListener { refresh() }
        refreshButton.performClick()
    }

    fun refresh() {
        connectionDisplay.text = DEBUG_STATS.CURRENT_MODE.toString()
        latLongDisplay.text = "lat: ${DEBUG_STATS.CURRENT_LATLONG?.first} long: ${DEBUG_STATS.CURRENT_LATLONG?.second}"
        trackNameDisplay.text = DEBUG_STATS.CURRENT_TRACK
    }
}
