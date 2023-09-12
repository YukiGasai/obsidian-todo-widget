package de.yukigasai.obsidiantodowidget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import de.yukigasai.obsidiantodowidget.util.ActionsConstants


class KeepAliveInterval private constructor(context: Context) {
    companion object {

        private const val KEEP_ALIVE_ID = 123123

        @Volatile
        private var instance: KeepAliveInterval? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: KeepAliveInterval(context).also { instance = it }
            }
    }

    init {
        create(context)
    }

    private fun create(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val triggerEveryMillis = 1 * 15 * 1000

        val updateWidgetIntent = Intent(context, TodoWidgetReceiver::class.java)
        updateWidgetIntent.action = ActionsConstants.UPDATE_WIDGET
        // Create a pending intent that will be triggered when the alarm goes off
        val updateWidgetPendingIntent = PendingIntent.getBroadcast(context,
            KEEP_ALIVE_ID, updateWidgetIntent, PendingIntent.FLAG_MUTABLE)
        println("SET KEEP ALIVE")
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), triggerEveryMillis.toLong(), updateWidgetPendingIntent )
    }


    fun clear(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val updateWidgetIntent = Intent(context, TodoWidgetReceiver::class.java)
        updateWidgetIntent.action = ActionsConstants.UPDATE_WIDGET

        // Create a pending intent that will be triggered when the alarm goes off
        val updateWidgetPendingIntent = PendingIntent.getBroadcast(context,
            KEEP_ALIVE_ID, updateWidgetIntent, PendingIntent.FLAG_MUTABLE)
        alarmManager.cancel(updateWidgetPendingIntent)
        println("CLEAR KEEP ALIVE")
    }


    fun update(context: Context) {
        println("UPDATE KEEP ALIVE")
        clear(context)
        create(context)
    }
}