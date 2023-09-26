package de.yukigasai.obsidiantodowidget

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.RECEIVER_EXPORTED
import androidx.core.content.ContextCompat.registerReceiver
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
import androidx.glance.appwidget.GlanceAppWidgetManager
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
import androidx.glance.text.TextAlign
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import de.yukigasai.obsidiantodowidget.reminder.ReminderEndReceiver
import de.yukigasai.obsidiantodowidget.reminder.ReminderRepo
import de.yukigasai.obsidiantodowidget.reminder.ReminderStartReceiver
import de.yukigasai.obsidiantodowidget.todo.TodoItem
import de.yukigasai.obsidiantodowidget.todo.TodoRepo
import de.yukigasai.obsidiantodowidget.util.ActionsConstants
import de.yukigasai.obsidiantodowidget.util.Counter
import de.yukigasai.obsidiantodowidget.util.KeysConstants
import de.yukigasai.obsidiantodowidget.util.OnScreenReceiver
import kotlinx.coroutines.runBlocking

class TodoWidget : GlanceAppWidget() {

    private var onScreenReceiver: OnScreenReceiver? = null

    fun removeOnScreenReceiver(context: Context) {
        println("removeOnScreenReceiver")
        if (onScreenReceiver != null) {
            context.unregisterReceiver(onScreenReceiver)
            onScreenReceiver = null
        }
    }

    fun registerOnScreenReceiver(context: Context) {
        Counter(context, false).increment()
        removeOnScreenReceiver(context)

        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_USER_PRESENT)

        onScreenReceiver = OnScreenReceiver()
        println("registerOnScreenReceiver")
        registerReceiver(context, onScreenReceiver, filter, RECEIVER_EXPORTED)
    }


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
        val config = WidgetConfig.loadFromPrefs(context)

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
                        // text = "${Counter(context, true).count} ${Counter(context, false).count} ${config.getArticleFileName().dropLast(3)}",
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

    fun GlanceModifier.leftPad(config: WidgetConfig, item: TodoItem): GlanceModifier {
        if (!config.hideDoneTasks) {
            return this.then(GlanceModifier.padding(
                start = (12 * (item.offSet.length - 1) + 8).dp,
                top = 4.dp,
                end = 4.dp,
                bottom = 4.dp,
            ))
        }
        return this.then(GlanceModifier.padding(4.dp))
    }

    if(item.getStateEmoji() == "") {

        val prefs = currentState<Preferences>()
        val checked = prefs[booleanPreferencesKey(item.id.toString())] ?: item.isChecked
        CheckBox(
            text = item.getTodoText(),
            style = TextStyle(GlanceTheme.colors.onBackground),
            checked = checked,
            colors = CheckboxDefaults.colors(),
            onCheckedChange = actionRunCallback<CheckboxClickAction>(
                actionParametersOf(
                    toggledItemKey to item.id.toString()
                ),
            ),
            modifier = GlanceModifier.leftPad(config, item)
        )
    } else {
        Row (
            verticalAlignment = Alignment.Vertical.CenterVertically,
        ){
            Text(
                text = item.getStateEmoji(),
                style = TextStyle(
                    fontSize = 18.sp,
                    textAlign = TextAlign.Right,
                    textDecoration = TextDecoration.None,
                    color = GlanceTheme.colors.onSurface,
                ),
                modifier = GlanceModifier.leftPad(config, item).then(
                GlanceModifier.clickable(actionRunCallback<CheckboxClickAction>(
                    actionParametersOf(
                        toggledItemKey to item.id.toString()
                    ),
                ))).then(GlanceModifier.width(30.dp))
            )
            Text(
                text = item.name,
                style = TextStyle(
                    fontSize = 15.sp,
                    color = GlanceTheme.colors.onSurface,
                ),
                modifier = GlanceModifier.clickable(actionRunCallback<CheckboxClickAction>(
                    actionParametersOf(
                        toggledItemKey to item.id.toString()
                    ),
                ))
            )
        }
    }
}

private val toggledItemKey = ActionParameters.Key<String>(KeysConstants.TOGGLE_ITEM_KEY)

class OpenObsidianAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {

        val config = WidgetConfig.loadFromPrefs(context)

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

        val config = WidgetConfig.loadFromPrefs(context)
        val toggledItemKey = requireNotNull(parameters[toggledItemKey])
        val checked = parameters[ToggleableStateKey]

        if (checked != null) {
            updateAppWidgetState(context, glanceId) { state ->
                state[booleanPreferencesKey(toggledItemKey)] = checked
            }
        }

        val toggledItem = TodoRepo.currentTodos.value[toggledItemKey.toInt()]
        toggledItem.updateInFile(config)
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
@SuppressLint("ScheduleExactAlarm")
suspend fun refreshTodos(context: Context, glanceId: GlanceId) {
    val config = WidgetConfig.loadFromPrefs(context)

    // Update the todos from the file
    TodoRepo.updateTodos(config)

    // Update the reminder from the todos
    ReminderRepo.getInstance().update(context, TodoRepo.currentTodos.value)

    updateAppWidgetState(context, glanceId) { state ->
        for(todo in TodoRepo.currentTodos.value) {
            state[booleanPreferencesKey(todo.id.toString())] = todo.isChecked
        }
    }

    // Redraw widget
    TodoWidget().update(context, glanceId)
}

class TodoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodoWidget()

    private suspend fun updateAllWidgets (context: Context) {
        val manager = GlanceAppWidgetManager(context)
        val widget = TodoWidget()
        val glanceIds = manager.getGlanceIds(widget.javaClass)
        glanceIds.forEach { glanceId ->
            refreshTodos(context, glanceId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        TodoWidget().removeOnScreenReceiver(context)
        Counter(context, true).reset()
        Counter(context, false).reset()
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ActionsConstants.START_NOTIFICATION -> ReminderStartReceiver.onReceive(context, intent)
            ActionsConstants.END_NOTIFICATION_CHECK -> {
                ReminderEndReceiver.onReceive(context, intent)
                runBlocking {
                    updateAllWidgets(context)
                }
            }
            ActionsConstants.UPDATE_WIDGET -> {
                runBlocking {
                    updateAllWidgets(context)
                }
            }
        }
    }


    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        TodoWidget().registerOnScreenReceiver(context)

        val manager = GlanceAppWidgetManager(context)
        runBlocking {
            val glanceIds = manager.getGlanceIds(TodoWidget().javaClass)
            glanceIds.forEach { glanceId ->
                refreshTodos(context, glanceId)
            }
        }
    }

}
