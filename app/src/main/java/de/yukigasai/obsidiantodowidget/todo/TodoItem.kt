package de.yukigasai.obsidiantodowidget.todo

import de.yukigasai.obsidiantodowidget.util.FsHelper
import de.yukigasai.obsidiantodowidget.WidgetConfig

data class TodoItem(val id: Int, val name:String, var isChecked: Boolean, var offSet: String) {
    fun updateInFile(config: WidgetConfig) {
        val originalFile = FsHelper.loadTextData(config)
        val newFileData: String = if(this.isChecked){
            val regex = Regex("- \\[x] ${this.name}\$", RegexOption.MULTILINE)
            originalFile.replace(regex, "- [ ] ${this.name}")
        }else {
            val regex = Regex("- \\[ ] ${this.name}\$", RegexOption.MULTILINE)
            originalFile.replace(regex, "- [x] ${this.name}")
        }
        FsHelper.overwriteFile(config.getFullPath(), newFileData)
    }

}