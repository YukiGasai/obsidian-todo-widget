package de.yukigasai.obsidianwidget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.CheckBox
import androidx.annotation.RequiresApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalGlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.Switch
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.ToggleableStateKey
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.coroutineContext


class TodoWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        refreshTodos(context, id)
        provideContent { Content() }
    }

    @Composable
    fun Content() {
        val todos by TodoRepo.currentTodos.collectAsState()
        val context = LocalContext.current
        val config = ListSharedPrefsUtil.loadWidgetSettings(context)
        GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .appWidgetBackground()
                        .padding(4.dp)
                        .background(androidx.glance.R.color.glance_colorBackground)
                ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(4.dp),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    val modifier = GlanceModifier.defaultWeight()
                    Text(
                        text = config.getFullPath().split("/").last().dropLast(3),
                        modifier = modifier.then(
                            GlanceModifier.clickable(actionRunCallback<OpenObsidianAction>())
                        ),
                        maxLines = 1,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ColorProvider(
                                androidx.glance.R.color.glance_colorPrimary
                            )
                        )
                    )
                    Image(
                        provider = ImageProvider(R.drawable.refresh),
                        contentDescription = "Refresh",
                        modifier = modifier
                            .clickable(actionRunCallback<RefreshButtonHandler>())
                            .padding(0.dp)
                            .width(24.dp)
                    )
                }
                if (todos.isEmpty()) {
                    Text(
                        text = "No todos",
                        style = TextStyle(
                            color = ColorProvider(androidx.glance.R.color.glance_colorSecondary)
                        )
                    )
                } else {
                    LazyColumn {
                        items(todos) { todoItem ->
                            CheckBoxItem(todoItem)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckBoxItem(item: TodoItem) {
    val prefs = currentState<Preferences>()
    val checked = prefs[booleanPreferencesKey(item.id.toString())] ?: item.isChecked
    CheckBox(

        text = item.name,
        checked = checked,
        onCheckedChange = actionRunCallback<CheckboxClickAction>(
            actionParametersOf(
                toggledItemKey to item.id.toString()
            ),
        ),
        modifier = GlanceModifier.padding(4.dp),

    )
}

private val toggledItemKey = ActionParameters.Key<String>("ToggledItemKey")

class OpenObsidianAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {

        val config = ListSharedPrefsUtil.loadWidgetSettings(context)

        Intent(Intent.ACTION_VIEW, Uri.parse(config.getFullObsidianURL())).apply {
            flags = FLAG_ACTIVITY_NEW_TASK
            context.startActivity(this)
        }
    }
}

class CheckboxClickAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {

        val config = ListSharedPrefsUtil.loadWidgetSettings(context)
        val toggledItemKey = requireNotNull(parameters[toggledItemKey])
        val checked = requireNotNull(parameters[ToggleableStateKey])

        updateAppWidgetState(context, glanceId) { state ->
            state[booleanPreferencesKey(toggledItemKey)] = checked
        }

        val toggledItem = TodoRepo.currentTodos.value[toggledItemKey.toInt()]
        FsHelper().updateTaskInFile(config.getFullPath(), toggledItem)

        refreshTodos(context, glanceId)
    }
}

class RefreshButtonHandler : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        refreshTodos(context, glanceId)
    }
}

/**
 * Force update the todo info after user click
 */

suspend fun refreshTodos(context: Context, glanceId: GlanceId) {
    val config = ListSharedPrefsUtil.loadWidgetSettings(context)

    TodoRepo.updateTodos(config)
    updateAppWidgetState(context, glanceId) { state ->
        for(todo in TodoRepo.currentTodos.value) {
            state[booleanPreferencesKey(todo.id.toString())] = todo.isChecked
        }
    }
    TodoWidget().update(context, glanceId)
}

class TodoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodoWidget()
}