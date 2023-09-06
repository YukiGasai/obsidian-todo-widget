package de.yukigasai.obsidiantodowidget

object Constants {
    object Actions {
        val START_NOTIFICATION     = "de.yukigasai.obsidiantodowidget.ACTION_START_NOTIFICATION"
        val END_NOTIFICATION_CHECK = "de.yukigasai.obsidiantodowidget.ACTION_END_NOTIFICATION_CHECK"
        val UPDATE_CONFIG          = "de.yukigasai.obsidiantodowidget.ACTION_UPDATE_CONFIG"
    }
    object Extras {
        val NOTIFICATION_TITLE   = "EXTRA_NOTIFICATION_TITLE"
        val NOTIFICATION_MESSAGE = "EXTRA_NOTIFICATION_MESSAGE"
        val NOTIFICATION_TODO    = "EXTRA_NOTIFICATION_TODO"
    }

    object Channel {
        val ID = "de.yukigasai.obsidiantodowidget.CHANNEL_REMINDER"
        val NAME = "Obsidian Todo Reminder"
    }

    object Keys {
        val TOGGLE_ITEM_KEY = "ToggledItemKey"
    }

    object Preferences {
        val WIDGET_CONFIG = "de.yukigasai.obsidiantodowidget.PREFERENCES_WIDGET_CONFIG"
        val PREFIX = "todoWidget"
    }
}