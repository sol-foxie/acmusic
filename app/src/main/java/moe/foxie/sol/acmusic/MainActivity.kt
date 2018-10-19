package moe.foxie.sol.acmusic

import android.app.Activity
import android.media.MediaPlayer
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val manager = MusicManager(this, jukebox, acnlTracks)
        jukebox.onItemSelectedListener = manager
    }

}

val acnlTracks = hashMapOf<Int,Int>(
    0 to R.raw.nl12am,
    1 to R.raw.nl2am,
    2 to R.raw.nl3am,
    3 to R.raw.nl4am,
    4 to R.raw.nl5am,
    5 to R.raw.nl6am,
    6 to R.raw.nl7am,
    7 to R.raw.nl8am,
    8 to R.raw.nl9am,
    9 to R.raw.nl10am,
    10 to R.raw.nl11am,
    11 to R.raw.nl12pm,
    12 to R.raw.nl1pm,
    13 to R.raw.nl2pm,
    14 to R.raw.nl3pm,
    15 to R.raw.nl4pm,
    16 to R.raw.nl5pm,
    17 to R.raw.nl6pm,
    18 to R.raw.nl7pm,
    19 to R.raw.nl8pm,
    20 to R.raw.nl9pm,
    21 to R.raw.nl10pm,
    22 to R.raw.nl11pm,
    23 to R.raw.nl1am
)
