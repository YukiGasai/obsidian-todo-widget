package de.yukigasai.obsidiantodowidget.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager.NameNotFoundException
import androidx.core.content.edit

private fun isDebuggable(ctx: Context): Boolean {
    var debuggable = false
    val pm = ctx.packageManager
    try {
        val appInfo = pm.getApplicationInfo(ctx.packageName, 0)
        debuggable = 0 != appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
    } catch (e: NameNotFoundException) {
        /*debuggable variable will remain false*/
    }
    return debuggable
}


class Counter(val context: Context, private val isOwn: Boolean) {
    var count: Int

    init {
        this.count = loadFromPrefs()
    }

    private fun loadFromPrefs(): Int {
        WidgetLogger.info("${if(isOwn) "TODOS" else "TIMER"} | Loading the count")
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
        WidgetLogger.info("${if(isOwn) "TODOS" else "TIMER"} | Increment the count to $count")
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
        WidgetLogger.info("${if(isOwn) "TODOS" else "TIMER"} | Reset the count")
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