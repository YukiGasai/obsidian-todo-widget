package de.yukigasai.obsidiantodowidget.todo

import de.yukigasai.obsidiantodowidget.util.FsHelper.loadTextData
import de.yukigasai.obsidiantodowidget.WidgetConfig

data class TodoFactory(val config: WidgetConfig) {

    // Optionally reduce the content of the file to the selected heading
    private fun filterFileContentForHeader(fileContent: String): String {
        if (config.header.isEmpty()) return fileContent

        val findHeader = Regex("^(#+)\\s${Regex.escape(config.header)}\$", RegexOption.MULTILINE)
        val match1 = findHeader.find(fileContent) ?: return ""
        val headerLevel = match1.groupValues[1].length
        var updatedFileContent = fileContent.drop(match1.range.last)

        val findNextHeader = if (config.includeSubHeader) {
            Regex("^#{1,${headerLevel}}\\s.*\$",
                setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL))
        } else {
            Regex("^#+\\s.*\$",
                setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL))
        }

        val match2 = findNextHeader.find(updatedFileContent) ?: return updatedFileContent
        updatedFileContent = updatedFileContent.substring(0, match2.range.first)
        return updatedFileContent
    }

    // Create list of todos from the content in the file
    private fun parseTodos(fileContent: String): List<TodoItem>{
        val filteredFileContent = filterFileContentForHeader(fileContent)
        return Regex("^([^\\S\\r\\n]*)- \\[(.)] ([^#\\n]*#?(\\d)?.*)\$", RegexOption.MULTILINE)
            .findAll(filteredFileContent)
            .mapIndexed  {
                    index, todo ->  TodoItem(
                        id = index,
                        name = todo.groupValues[3],
                        state = todo.groupValues[2],
                        isChecked = todo.groupValues[2] == "x" || todo.groupValues[2] == "X",
                        offSet = todo.groupValues[1],
                        count = todo.groupValues[2].toIntOrNull() ?: 0,
                        repeats = todo.groupValues[4].toIntOrNull(),
                    )
            }.toList()
    }

    // Public function to get the Todolist
    fun getTodosFromFile(): List<TodoItem>{
        val fileContent = loadTextData(config)
        return parseTodos(fileContent)
    }
}