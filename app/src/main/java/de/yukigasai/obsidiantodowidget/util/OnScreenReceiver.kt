package de.yukigasai.obsidiantodowidget.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.yukigasai.obsidiantodowidget.TodoWidgetReceiver

class OnScreenReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        println("OnScreenReceiver ${intent.action}")

        Counter(context, true).increment()

        val resultData = Intent(context, TodoWidgetReceiver::class.java)
        resultData.action = ActionsConstants.UPDATE_WIDGET
        context.sendBroadcast(Intent(resultData))
    }

}