package de.yukigasai.obsidiantodowidget

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson

object ListSharedPrefsUtil {
    internal fun saveWidgetSettings(
        context: Context,
        widgetConfig: WidgetConfig,
    ) {
        val gson = Gson()
        val configAsJson = gson.toJson(widgetConfig)
        context.getSharedPreferences(Constants.Preferences.WIDGET_CONFIG, 0).edit {
            putString(Constants.Preferences.PREFIX, configAsJson)
        }
    }

    internal fun loadWidgetSettings(context: Context): WidgetConfig {
        val gson = Gson()
        val defaultConfig = WidgetConfig()
        val configAsJson = context.getSharedPreferences(Constants.Preferences.WIDGET_CONFIG, 0)
            .getString(Constants.Preferences.PREFIX, gson.toJson(defaultConfig))
        return gson.fromJson(configAsJson, WidgetConfig::class.java)
    }
}