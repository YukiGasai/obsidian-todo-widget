package de.yukigasai.obsidiantodowidget.reminder

import de.yukigasai.obsidiantodowidget.todo.TodoItem
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class ReminderFactory () {
    fun createFromTodo(todo: TodoItem): Reminder? {
        if (todo.isChecked) return null

        val nowDateTime = LocalDateTime.now()

        // Get The dateTime from the name
        val getDateRegex =
            Regex("[@\uD83D\uDCC5]\\{?((\\d{4}-\\d{2}-\\d{2})?(\\s?\\d{2}:\\d{2})?)\\}?")
        // Find date match or return
        val match: MatchResult = getDateRegex.find(todo.name) ?: return null

        var dateString =
            match.groups[2]?.value ?: nowDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        dateString += if (match.groups[3] == null) {
            " 12:00"
        } else {
            " ${match.groups[3]?.value?.trim()}"
        }

        // Calculate the milliseconds  until reminder should fire
        val reminderDateTime: LocalDateTime
        try {
            reminderDateTime =
                LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        } catch (error: DateTimeParseException) {
            System.err.println(error.cause)
            return null
        }

        val duration = Duration.between(nowDateTime, reminderDateTime)
        // Event already happened
        if (duration.isNegative || duration.isZero) return null

        val durationMillis = duration.toMillis()
        val todoTileWithoutDate = todo.name.replace(match.groupValues[0], "")

        return Reminder(
            todo = todo,
            title = todoTileWithoutDate,
            // Millis until Reminder
            date = System.currentTimeMillis() + durationMillis,
            dateText = match.groupValues[0]
        )
    }
}