package de.yukigasai.obsidiantodowidget

import android.os.Environment
import java.io.File
import java.nio.charset.Charset
import kotlin.text.Regex.Companion.escape


class FsHelper {
    fun getTodosFromFile(config: WidgetConfig): List<TodoItem>{
        val fileContent = loadTextData(config)
        return parseTodos(config, fileContent)
    }

    private fun loadTextData(config: WidgetConfig): String {
        try {
            val file = File(
                Environment.getExternalStorageDirectory()
                    .absolutePath, config.getFullPath()
            )
            if (!file.exists()) return ""
            return file.readText(Charset.defaultCharset())
        }catch(err: Exception) {
            println(err)
            return ""
        }
    }

    private fun filterFileContentForHeader(config: WidgetConfig, fileContent: String): String {
        if (config.header.isEmpty()) return fileContent

        val findHeader = Regex("^(#+)\\s${escape(config.header)}\$", RegexOption.MULTILINE)
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

    private fun parseTodos(config: WidgetConfig, fileContent: String): List<TodoItem>{
        val filteredFileContent = filterFileContentForHeader(config, fileContent)
        return Regex("^([^\\S\\r\\n]*)- \\[([ x])] (.*)\$", RegexOption.MULTILINE)
            .findAll(filteredFileContent)
            .mapIndexed  {
                    index, todo ->  TodoItem(index, todo.groupValues[3], todo.groupValues[2] != " ", todo.groupValues[1])
            }.toList()
    }

    private fun overwriteFile(fileName: String, content: String) {
        val file = File(
            Environment.getExternalStorageDirectory()
                .absolutePath, fileName
        )
        file.writeText(content, Charset.defaultCharset())
    }

    fun updateTaskInFile(config: WidgetConfig, item: TodoItem) {
        val originalFile = FsHelper().loadTextData(config)
        val newFileData: String
        if(item.isChecked){
            val regex = Regex("- \\[x] ${item.name}\$", RegexOption.MULTILINE)
            newFileData = originalFile.replace(regex, "- [ ] ${item.name}")
        }else {
            val regex = Regex("- \\[ ] ${item.name}\$", RegexOption.MULTILINE)
            newFileData = originalFile.replace(regex, "- [x] ${item.name}")
        }
        overwriteFile(config.getFullPath(), newFileData)
    }

}