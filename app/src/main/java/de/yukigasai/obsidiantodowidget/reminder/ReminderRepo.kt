package de.yukigasai.obsidiantodowidget.reminder

import android.app.AlarmManager
import android.content.Context
import de.yukigasai.obsidiantodowidget.todo.TodoItem

class ReminderRepo private constructor() {
    companion object {

        @Volatile
        private var instance: ReminderRepo? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: ReminderRepo().also { instance = it }
            }
    }

    private val reminderList = mutableListOf<Reminder>()

    private fun clearAll(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (reminder in reminderList) {
            // Cancel alarm if pending intent is not null
            reminder.pendingIntent?.let { alarmManager.cancel(it) }
        }
        reminderList.clear()
    }


    fun update(context: Context, todoItems: List<TodoItem>){
        clearAll(context)
        val reminderFactory = ReminderFactory()

        for (todoItem in todoItems) {
            val reminder = reminderFactory.createFromTodo(todoItem)
            if (reminder != null) {
                reminder.register(context)
                reminderList.add(reminder)
            }
        }


    }

}