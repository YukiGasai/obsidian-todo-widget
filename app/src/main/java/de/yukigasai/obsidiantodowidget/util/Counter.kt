package de.yukigasai.obsidiantodowidget.util

import android.content.Context
import androidx.core.content.edit

class Counter(val context: Context, private val isOwn: Boolean) {
    var count: Int

    init {
        this.count = loadFromPrefs()
    }

    private fun loadFromPrefs(): Int {
        return if(isOwn) {
            context.getSharedPreferences(PreferencesConstants.OWN_COUNTER, 0)
                .getInt(PreferencesConstants.PREFIX, 0)
        } else {
            context.getSharedPreferences(PreferencesConstants.REAL_COUNTER, 0)
                .getInt(PreferencesConstants.PREFIX, 0)
        }
    }

    fun increment() {
        count += 1
        if(isOwn) {
            context.getSharedPreferences(PreferencesConstants.OWN_COUNTER, 0).edit {
                putInt(PreferencesConstants.PREFIX, count)
            }
        } else {
            context.getSharedPreferences(PreferencesConstants.REAL_COUNTER, 0).edit {
                putInt(PreferencesConstants.PREFIX, count)
            }
        }
    }

    fun reset() {
        count = 0
        if(isOwn) {
            context.getSharedPreferences(PreferencesConstants.OWN_COUNTER, 0).edit {
                putInt(PreferencesConstants.PREFIX, count)
            }
        } else {
            context.getSharedPreferences(PreferencesConstants.REAL_COUNTER, 0).edit {
                putInt(PreferencesConstants.PREFIX, count)
            }
        }
    }

}