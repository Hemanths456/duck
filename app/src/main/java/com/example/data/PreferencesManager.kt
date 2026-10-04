package com.example.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "duck_shooter_prefs"
        private const val KEY_BEST_SCORE = "key_best_score"
        private const val KEY_SOUND_ENABLED = "key_sound_enabled"
    }

    var bestScore: Int
        get() = prefs.getInt(KEY_BEST_SCORE, 0)
        set(value) {
            prefs.edit().putInt(KEY_BEST_SCORE, value).apply()
        }

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()
        }
}
