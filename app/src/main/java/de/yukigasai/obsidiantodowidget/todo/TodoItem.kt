package de.yukigasai.obsidiantodowidget.todo

import de.yukigasai.obsidiantodowidget.util.FsHelper
import de.yukigasai.obsidiantodowidget.WidgetConfig
import kotlin.text.Regex.Companion.escape

data class TodoItem(val id: Int, val name:String, var isChecked: Boolean, var offSet: String) {
    fun updateInFile(config: WidgetConfig) {
        val originalFile = FsHelper.loadTextData(config)
        val newFileData: String = if(this.isChecked){
            val regex = Regex("- \\[x] ${escape(this.name)}\$", RegexOption.MULTILINE)
            originalFile.replace(regex, "- [ ] ${this.name}")
        }else {
            val regex = Regex("- \\[ ] ${escape(this.name)}\$", RegexOption.MULTILINE)
            originalFile.replace(regex, "- [x] ${this.name}")
        }
        FsHelper.overwriteFile(config.getFullPath(), newFileData)
    }

}