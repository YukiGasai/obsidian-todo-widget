package de.yukigasai.obsidiantodowidget.util

import android.annotation.SuppressLint
import android.os.Environment
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar

object WidgetLogger {

    private var LOG_BASE_DIR = Environment.getExternalStorageDirectory().absolutePath + "/Documents/todo-widget-log/"

    private val LOG_PATH = mapOf(
        Log.INFO to "${LOG_BASE_DIR}info.txt",
        Log.WARN to "${LOG_BASE_DIR}warn.txt",
        Log.ERROR to "${LOG_BASE_DIR}error.txt"
    )

    fun info(msg: String) {
        log(Log.INFO, msg)
    }

    fun warn(msg: String) {
        log(Log.WARN, msg)
    }

    fun error(msg: String) {
        log(Log.ERROR, msg)
    }

    @SuppressLint("SimpleDateFormat")
    fun log(priority: Int, msg: String) {

        val time = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val current = formatter.format(time)
        val logEntry = "\n[$current]:$msg"

        Log.println(priority, "ObsidianWidgetLOG", logEntry)

        val logPath = LOG_PATH[priority] ?: return

        val logFile = File(logPath)

        if(!logFile.exists()) {

            // Check if base folder exists
            val baseLogFolder = File(LOG_BASE_DIR)
            if(!baseLogFolder.exists()) {
                baseLogFolder.mkdirs()
            }

            // Create logFile
            logFile.printWriter().use {out ->
                out.println(logEntry)
            }
        }else {
            logFile.appendText(logEntry)
        }
    }
}