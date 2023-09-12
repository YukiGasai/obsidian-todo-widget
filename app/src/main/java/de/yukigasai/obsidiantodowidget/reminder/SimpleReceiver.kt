package de.yukigasai.obsidiantodowidget.reminder

import android.content.Context
import android.content.Intent

interface SimpleReceiver {
    fun onReceive(context: Context, intent: Intent)
}