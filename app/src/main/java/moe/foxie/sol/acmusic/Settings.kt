package moe.foxie.sol.acmusic

import android.app.Activity
import android.os.Bundle

class Settings : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        actionBar.setDisplayHomeAsUpEnabled(true)
    }
}
