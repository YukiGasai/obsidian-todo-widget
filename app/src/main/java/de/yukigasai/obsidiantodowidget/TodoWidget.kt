package de.yukigasai.obsidiantodowidget

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
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
import androidx.glance.text.TextStyle
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

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
            start = (12 * (item.offSet.length - 1) + 8).dp,
            top = 4.dp,
            end = 4.dp,
            bottom = 4.dp,
        ) else GlanceModifier.padding(4.dp),
    )
}

private val toggledItemKey = ActionParameters.Key<String>(Constants.Keys.TOGGLE_ITEM_KEY)

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
        FsHelper().updateTaskInFile(config, toggledItem)

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
@SuppressLint("ScheduleExactAlarm")
fun checkForReminders(todo: TodoItem, context: Context) {
    val nowDateTime = LocalDateTime.now()

    // Get The dateTime from the name
    val getDateRegex = Regex("[@\uD83D\uDCC5]\\{?((\\d{4}-\\d{2}-\\d{2})?(\\s?\\d{2}:\\d{2})?)\\}?")
    // Find date match or return
    val match: MatchResult = getDateRegex.find(todo.name) ?: return


    var dateString = match.groups[2]?.value ?: nowDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    dateString += if(match.groups[3] == null){
        " 12:00"
    }else {
        " ${match.groups[3]?.value?.trim()}"
    }

    val todoTileWithoutDate = todo.name.replace(match.groupValues[0], "")
    // Calculate the milliseconds  until reminder should fire
    val reminderDateTime:LocalDateTime
    try {
        reminderDateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    }catch (error: DateTimeParseException) {
        System.err.println(error.cause)
        return
    }
    val duration = Duration.between(nowDateTime, reminderDateTime)

    // Event already happened
    if(duration.isNegative || duration.isZero) return

    val durationMillis = duration.toMillis()

    val reminderMillis = System.currentTimeMillis() + durationMillis

    // Hash the text of task to get a semi unique id
    // TODO: Find something more reliable
    val todoId = todo.name.hashCode()
    val intent = Intent(context, MyReceiver::class.java)
    val gson = Gson()
    // Set the action and the data for the intent
    intent.action = Constants.Actions.START_NOTIFICATION
    intent.putExtra(Constants.Extras.NOTIFICATION_TITLE, todoTileWithoutDate)
    intent.putExtra(Constants.Extras.NOTIFICATION_MESSAGE, match.groupValues[0])
    intent.putExtra(Constants.Extras.NOTIFICATION_TODO, gson.toJson(todo))

    // Create a pending intent that will be triggered when the alarm goes off
    val pendingIntent = PendingIntent.getBroadcast(context, todoId, intent, PendingIntent.FLAG_MUTABLE)
    // Get the alarm manager from the system
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Caner possibly running alarm for checked task
    if(todo.isChecked) {
        alarmManager.cancel(pendingIntent)
    } else {
        // Set alarm for task: will not set a new one if one with same todoId is already set
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderMillis, pendingIntent)
        println("Set alarm for ${todo.name} in ${duration.seconds} seconds")
    }
}

/**
 * Force update the todo info after user click
 */
@SuppressLint("ScheduleExactAlarm")
suspend fun refreshTodos(context: Context, glanceId: GlanceId) {
    val config = ListSharedPrefsUtil.loadWidgetSettings(context)

    TodoRepo.updateTodos(config)
    updateAppWidgetState(context, glanceId) { state ->
        for(todo in TodoRepo.currentTodos.value) {
            state[booleanPreferencesKey(todo.id.toString())] = todo.isChecked
            checkForReminders(todo, context)
        }
    }
    TodoWidget().update(context, glanceId)
}

class TodoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodoWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val gson = Gson()

        if(intent.action == Constants.Actions.END_NOTIFICATION_CHECK) {
            val config = ListSharedPrefsUtil.loadWidgetSettings(context)

            val todo = gson.fromJson(
                intent.getStringExtra(Constants.Extras.NOTIFICATION_TODO),
                TodoItem::class.java
            )
            FsHelper().updateTaskInFile(config, todo)
            val toast = Toast(context)
            toast.setText("Marked ${todo.name} as done.")
            toast.duration = Toast.LENGTH_SHORT
            toast.show()
            NotificationManagerCompat.from(context).cancel(null, todo.name.hashCode())
        }

        val manager = GlanceAppWidgetManager(context)
        val widget = TodoWidget()

        runBlocking {
            val glanceIds = manager.getGlanceIds(widget.javaClass)
            glanceIds.forEach { glanceId ->
                refreshTodos(context, glanceId)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        val manager = GlanceAppWidgetManager(context)
        val widget = TodoWidget()

        runBlocking {
            val glanceIds = manager.getGlanceIds(widget.javaClass)
            glanceIds.forEach { glanceId ->
                refreshTodos(context, glanceId)
            }
        }
    }

}
