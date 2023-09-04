package de.yukigasai.obsidianwidget

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.glance.BackgroundModifier
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.CheckboxDefaults
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.ToggleableStateKey
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

class TodoWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        refreshTodos(context, id)
        provideContent { Content() }
    }

    private fun GlanceModifier.appWidgetBackgroundCornerRadius(): GlanceModifier {
        if (Build.VERSION.SDK_INT >= 31) {
            return cornerRadius(android.R.dimen.system_app_widget_background_radius)
        }

        return this.then(
            BackgroundModifier(
                ImageProvider(R.drawable.background_widget), ContentScale.Fit)
        )
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
                    .background(GlanceTheme.colors.background)
                    .appWidgetBackgroundCornerRadius()
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(4.dp),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    val modifier = GlanceModifier.defaultWeight()
                    Text(
                        text = config.getArticleFileName().dropLast(3),
                        modifier = modifier.then(
                            GlanceModifier.clickable(actionRunCallback<OpenObsidianAction>())
                        ),
                        maxLines = 1,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = GlanceTheme.colors.primary
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
                            color = GlanceTheme.colors.onBackground
                        )
                    )
                } else {
                    LazyColumn {
                        items(todos) { todoItem ->
                            if(!todoItem.isChecked || !config.hideDoneTasks) {
                                CheckBoxItem(todoItem, config)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckBoxItem(item: TodoItem, config: WidgetConfig) {
    val prefs = currentState<Preferences>()
    val checked = prefs[booleanPreferencesKey(item.id.toString())] ?: item.isChecked
    item.offSet = item.offSet.ifEmpty { " " }

    CheckBox(
        text = item.name,
        style = TextStyle(GlanceTheme.colors.onBackground),
        checked = checked,
        colors = CheckboxDefaults.colors(),
        onCheckedChange = actionRunCallback<CheckboxClickAction>(
            actionParametersOf(
                toggledItemKey to item.id.toString()
            ),
        ),
        modifier = if (!config.hideDoneTasks) GlanceModifier.padding(
            start = (12 * (item.offSet.length - 1)).dp,
            top = 4.dp,
            end = 4.dp,
            bottom = 4.dp,
        ) else GlanceModifier.padding(4.dp),
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
