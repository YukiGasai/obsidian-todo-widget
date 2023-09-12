package de.yukigasai.obsidiantodowidget.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Environment
import androidx.core.content.edit
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar

private fun isDebuggable(ctx: Context): Boolean {
    var debuggable = false
    val pm = ctx.packageManager
    try {
        val appinfo = pm.getApplicationInfo(ctx.packageName, 0)
        debuggable = 0 != appinfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
    } catch (e: NameNotFoundException) {
        /*debuggable variable will remain false*/
    }
    return debuggable
}


class Counter(val context: Context, private val isOwn: Boolean) {
    var count: Int

    private var LOG_PATH = "/Download/todo-widget-log.txt"

    init {
        this.count = loadFromPrefs()
    }

    @SuppressLint("SimpleDateFormat")
    private fun updateLog(msg: String) {

        if (!isDebuggable(context)) {
            return
        }

        val time = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val current = formatter.format(time)
        val logEntry = "\n[$current]:$msg"

        val logFile = File(Environment.getExternalStorageDirectory().absolutePath, LOG_PATH)
        if (!logFile.exists()) {
            logFile.printWriter().use {out ->
                out.println(logEntry)
            }
        }else {
            logFile.appendText(logEntry)
        }
    }

    private fun loadFromPrefs(): Int {
        updateLog("${if(isOwn) "TODOS" else "TIMER"} | Loading the count")
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
        updateLog("${if(isOwn) "TODOS" else "TIMER"} | Increment the count to $count")
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
        updateLog("${if(isOwn) "TODOS" else "TIMER"} | Reset the count")
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