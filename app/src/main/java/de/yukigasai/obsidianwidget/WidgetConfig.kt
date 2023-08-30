package de.yukigasai.obsidianwidget

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class WidgetConfig(
    var folder: String = "",
    var fileName: String = "",
    var useRegex: Boolean = false,
    var vaultName: String = "",
    var hideDoneTasks: Boolean = false
) {
    fun getFullPath(): String {
        if(!useRegex) {
            return "${folder}${fileName}.md"
        }

        val formatter = DateTimeFormatter.ofPattern(fileName)
        val currentFileName = LocalDateTime.now().format(formatter)
        return "${folder}${currentFileName}.md"
    }

    fun getFullObsidianURL(): String {
        val getInternalPathToFile = getFullPath().split(vaultName).last()
        return "obsidian://open?vault=${vaultName}&file=${getInternalPathToFile}"
    }

    fun isConfigured(): Boolean {
        return folder.isNotBlank() && fileName.isNotBlank() && vaultName.isNotBlank()
    }
}