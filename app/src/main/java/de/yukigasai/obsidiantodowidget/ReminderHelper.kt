package de.yukigasai.obsidiantodowidget

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class ReminderInfo (val reminderTriggerMillis: Long, val reminderTitle: String, val reminderDate: String)
class ReminderHelper {
    companion object {
        private val alarmList: MutableList<PendingIntent> = arrayListOf()
    fun clearAllReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (pendingIntent in alarmList) {
            // Dummy Pending intent with todoId
            alarmManager.cancel(pendingIntent)
        }
        alarmList.clear()
    }

    @SuppressLint("ScheduleExactAlarm")
    fun createReminderForTodo(context: Context, todo: TodoItem) {
        // Don't create reminder for tasks that are  done
        if (todo.isChecked) return

        // Get the time when a reminder should fire
        val reminderData = checkIfTodoWhenReminderDate(todo) ?: return

        // Hash the text of task to get a semi unique id
        // TODO: Find something more reliable
        val todoId = todo.name.hashCode()
        val intent = Intent(context, MyReceiver::class.java)
        val gson = Gson()
        // Set the action and the data for the intent
        intent.action = Constants.Actions.START_NOTIFICATION
        intent.putExtra(Constants.Extras.NOTIFICATION_TITLE, reminderData.reminderTitle)
        intent.putExtra(Constants.Extras.NOTIFICATION_MESSAGE, reminderData.reminderDate)
        intent.putExtra(Constants.Extras.NOTIFICATION_TODO, gson.toJson(todo))

        // Create a pending intent that will be triggered when the alarm goes off
        val pendingIntent = PendingIntent.getBroadcast(context, todoId, intent, PendingIntent.FLAG_MUTABLE)
        // Get the alarm manager from the system
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Set alarm for task: will not set a new one if one with same todoId is already set
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderData.reminderTriggerMillis, pendingIntent)
        alarmList.add(pendingIntent)
    }

    private fun checkIfTodoWhenReminderDate(todo: TodoItem): ReminderInfo? {
        val nowDateTime = LocalDateTime.now()

        // Get The dateTime from the name
        val getDateRegex = Regex("[@\uD83D\uDCC5]\\{?((\\d{4}-\\d{2}-\\d{2})?(\\s?\\d{2}:\\d{2})?)\\}?")
        // Find date match or return
        val match: MatchResult = getDateRegex.find(todo.name) ?: return null

        var dateString = match.groups[2]?.value ?: nowDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        dateString += if(match.groups[3] == null){
            " 12:00"
        }else {
            " ${match.groups[3]?.value?.trim()}"
        }

        // Calculate the milliseconds  until reminder should fire
        val reminderDateTime:LocalDateTime
        try {
            reminderDateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        }catch (error: DateTimeParseException) {
            System.err.println(error.cause)
            return null
        }

        val duration = Duration.between(nowDateTime, reminderDateTime)
        // Event already happened
        if(duration.isNegative || duration.isZero) return null

        val durationMillis = duration.toMillis()
        println("Set alarm for ${todo.name} in ${duration.seconds} seconds")
        val todoTileWithoutDate = todo.name.replace(match.groupValues[0], "")
        // Millis until Reminder
        return ReminderInfo(System.currentTimeMillis() + durationMillis, todoTileWithoutDate, match.groupValues[0])
    }
    }
}