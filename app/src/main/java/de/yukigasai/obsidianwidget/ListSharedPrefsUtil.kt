package de.yukigasai.obsidianwidget

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson


object ListSharedPrefsUtil {
    private const val PREFS_NAME = "de.yukigasai.obsidianwidget.TodoWidget"
    private const val PREF_PREFIX_KEY = "todoWidget"

    internal fun saveWidgetSettings(
        context: Context,
        widgetConfig: WidgetConfig,
    ) {
        val gson = Gson()
        val configAsJson = gson.toJson(widgetConfig)
        context.getSharedPreferences(PREFS_NAME, 0).edit {
            putString(PREF_PREFIX_KEY, configAsJson)
        }
    }

    internal fun loadWidgetSettings(context: Context): WidgetConfig {
        val gson = Gson()
        val defaultConfig = WidgetConfig()
        val configAsJson = context.getSharedPreferences(PREFS_NAME, 0)
            .getString(PREF_PREFIX_KEY, gson.toJson(defaultConfig))
        return gson.fromJson(configAsJson, WidgetConfig::class.java)
    }
}