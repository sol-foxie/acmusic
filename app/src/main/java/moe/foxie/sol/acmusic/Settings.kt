package moe.foxie.sol.acmusic

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_settings.*

class Settings : Activity() {

    private lateinit var listener: SharedPreferences.OnSharedPreferenceChangeListener
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        actionBar.setDisplayHomeAsUpEnabled(true)

        prefs = getSharedPreferences(PREFERENCES,Context.MODE_PRIVATE)
        listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key -> if (key == SOUNDTRACK_PREFERENCE) updateSelectedButton() }


        acnlButton.setOnClickListener { saveSoundtrack(SOUNDTRACK.NEW_LEAF)        }
        acwwButton.setOnClickListener { saveSoundtrack(SOUNDTRACK.WILD_WORLD)      }
        acgcButton.setOnClickListener { saveSoundtrack(SOUNDTRACK.ANIMAL_CROSSING) }

        prefs.registerOnSharedPreferenceChangeListener(listener)
        updateSelectedButton()

        attributionsButton.setOnClickListener {
            val attributionsIntent = Intent(this, Attributions::class.java)
            startActivity(attributionsIntent)
        }
    }

    private fun saveSoundtrack(soundtrack: Int) {
        prefs.edit().putInt(SOUNDTRACK_PREFERENCE, soundtrack).apply()
    }

    private fun updateSelectedButton() {
        val soundtrack = prefs.getInt(SOUNDTRACK_PREFERENCE,SOUNDTRACK.NEW_LEAF)
        for (b in arrayOf(acnlButton,acwwButton,acgcButton)) b.isEnabled = true

        when (soundtrack) {
            SOUNDTRACK.NEW_LEAF -> acnlButton
            SOUNDTRACK.WILD_WORLD -> acwwButton
            SOUNDTRACK.ANIMAL_CROSSING -> acgcButton
            else -> null
        }?.isEnabled = false
    }
}
