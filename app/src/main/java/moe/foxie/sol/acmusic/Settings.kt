package moe.foxie.sol.acmusic

import android.app.Activity
import android.content.Context
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
        accfButton.setOnClickListener { saveSoundtrack(SOUNDTRACK.CITY_FOLK)       }
        acwwButton.setOnClickListener { saveSoundtrack(SOUNDTRACK.WILD_WORLD)      }
        acgcButton.setOnClickListener { saveSoundtrack(SOUNDTRACK.ANIMAL_CROSSING) }
        ac64Button.setOnClickListener { saveSoundtrack(SOUNDTRACK.ANIMAL_FOREST)   }

        prefs.registerOnSharedPreferenceChangeListener(listener)
        updateSelectedButton()
    }

    private fun saveSoundtrack(soundtrack: Int) {
        prefs.edit().putInt(SOUNDTRACK_PREFERENCE, soundtrack).apply()
    }

    private fun updateSelectedButton() {
        val soundtrack = prefs.getInt(SOUNDTRACK_PREFERENCE,SOUNDTRACK.NEW_LEAF)
        for (b in arrayOf(acnlButton,accfButton,acwwButton,acgcButton,ac64Button)) b.isEnabled = true

        when (soundtrack) {
            SOUNDTRACK.NEW_LEAF -> acnlButton
            SOUNDTRACK.CITY_FOLK -> accfButton
            SOUNDTRACK.WILD_WORLD -> acwwButton
            SOUNDTRACK.ANIMAL_CROSSING -> acgcButton
            SOUNDTRACK.ANIMAL_FOREST -> ac64Button
            else -> null
        }?.isEnabled = false
    }
}
