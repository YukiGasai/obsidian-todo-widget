package de.yukigasai.obsidiantodowidget

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


data class WidgetConfig(
    var folder: String = "",
    var fileName: String = "",
    var vaultName: String = "",
    var hideDoneTasks: Boolean = false
) {
    private fun replacePattern(today: LocalDateTime, pattern: String): String{
        val trueDatePattern = pattern.drop(2).dropLast(2)
        val formatter = DateTimeFormatter.ofPattern(trueDatePattern)
        return today.format(formatter)
    }

    private fun String.checkForPatterns(): String {
        // Filename has special date placeholder
        val today:LocalDateTime = LocalDateTime.now()
        // Replace patterns with values for current day
        return Regex("\\{\\{[^}]*\\}\\}").replace(this) {
                result -> replacePattern(today, result.value)
        }
    }

    fun getArticleFileName(): String {
        // Remove filename if it is entered.
        if(fileName.endsWith(".md")) {
            fileName = fileName.dropLast(3)
        }

        return "${fileName.checkForPatterns()}.md"
    }

    private fun getFolderPath(): String {
        //Make sure path is formatted right
        if(!folder.endsWith("/")) {
            folder = "${folder}/"
        }

        if(!folder.startsWith("/")) {
            folder = "/${folder}"
        }

        return folder.checkForPatterns()
    }

    fun getFullPath(): String {

        return "${getFolderPath()}${getArticleFileName()}"
    }

    fun getFullObsidianURL(): String {
        val getInternalPathToFile = getFullPath().split(vaultName).last()
        return "obsidian://open?vault=${vaultName}&file=${getInternalPathToFile}"
    }

    fun isConfigured(): Boolean {
        return folder.isNotBlank() && fileName.isNotBlank() && vaultName.isNotBlank()
    }
}