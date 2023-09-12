package de.yukigasai.obsidiantodowidget.util

object ActionsConstants {
    const val START_NOTIFICATION     = "de.yukigasai.obsidiantodowidget.ACTION_START_NOTIFICATION"
    const val END_NOTIFICATION_CHECK = "de.yukigasai.obsidiantodowidget.ACTION_END_NOTIFICATION_CHECK"
    const val UPDATE_WIDGET         =  "android.appwidget.action.APPWIDGET_UPDATE"
}
object ExtrasConstants {
    const val NOTIFICATION_TITLE   = "EXTRA_NOTIFICATION_TITLE"
    const val NOTIFICATION_MESSAGE = "EXTRA_NOTIFICATION_MESSAGE"
    const val NOTIFICATION_TODO    = "EXTRA_NOTIFICATION_TODO"
}

object ChannelConstants {
    const val ID = "de.yukigasai.obsidiantodowidget.CHANNEL_REMINDER"
    const val NAME = "Obsidian Todo Reminder"
}

object KeysConstants {
    const val TOGGLE_ITEM_KEY = "ToggledItemKey"
}

object PreferencesConstants {
    const val PREFIX = "todoWidget"
    const val WIDGET_CONFIG = "de.yukigasai.obsidiantodowidget.PREFERENCES_WIDGET_CONFIG"

    const val OWN_COUNTER = "de.yukigasai.obsidiantodowidget.PREFERENCES_WIDGET_DEBUG_OWN_COUNTER"
    const val REAL_COUNTER = "de.yukigasai.obsidiantodowidget.PREFERENCES_WIDGET_DEBUG_REAL_COUNTER"

}
