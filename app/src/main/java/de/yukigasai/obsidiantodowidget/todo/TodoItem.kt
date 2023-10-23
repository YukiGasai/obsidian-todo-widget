package de.yukigasai.obsidiantodowidget.todo

import de.yukigasai.obsidiantodowidget.WidgetConfig
import de.yukigasai.obsidiantodowidget.util.FsHelper
import kotlin.text.Regex.Companion.escape

data class TodoItem(val id: Int, val name:String, val state:String, var isChecked: Boolean, var offSet: String, var count: Int = 0, val repeats: Int?) {
    fun updateInFile(config: WidgetConfig) {
        val originalFile = FsHelper.loadTextData(config)

        // Allow items to be unchecked if they are already set to done
        if(this.isChecked) {
            val regex = Regex("- \\[[xX]] ${escape(this.name)}\$", RegexOption.MULTILINE)
            val newFileData = originalFile.replace(regex, "- [ ] ${this.name}")
            FsHelper.overwriteFile(config.getFullPath(), newFileData)
            return
        }

        // If not repeat allow item to be checked
        if(this.repeats == null && !this.isChecked) {
            val regex = Regex("- \\[[^xX]] ${escape(this.name)}\$", RegexOption.MULTILINE)
            val newFileData = originalFile.replace(regex, "- [x] ${this.name}")
            FsHelper.overwriteFile(config.getFullPath(), newFileData)
            return
        }

        this.count+=1

        // Check item if the repeat count is reached
        if(this.repeats != null && this.count >= this.repeats) {
            val regex = Regex("- \\[[^xX]] ${escape(this.name)}\$", RegexOption.MULTILINE)
            val newFileData = originalFile.replace(regex, "- [x] ${this.name}")
            FsHelper.overwriteFile(config.getFullPath(), newFileData)
            return
        }

        // Increment the count if item is clicked
        if(this.repeats != null && this.count < this.repeats) {
            val regex = Regex("- \\[[^xX]] ${escape(this.name)}\$", RegexOption.MULTILINE)
            val newFileData = originalFile.replace(regex, "- [${this.count}] ${this.name}")
            FsHelper.overwriteFile(config.getFullPath(), newFileData)
            return
        }
    }

    fun getStateEmoji(): String {
        if(this.state.lowercase() == "x") {
            return ""
        }

        if(this.repeats != null) {
            return getEmojiForNumber(this.count)
        }

        when (this.state) {
            "/" -> {
                return "🔃"
            }
            "-" -> {
                return "✖️"
            }
            ">" -> {
                return "➡️"
            }
            "<" -> {
                return "📅"
            }
            "?" -> {
                return "❓"
            }
            "!" -> {
                return "❗"
            }
            "\"" -> {
                return "💬"
            }
            "l" -> {
                return "📍"
            }
            "b" -> {
                return "🔖"
            }
            "i" -> {
                return "ℹ️"
            }
            "S" -> {
                return "💲"
            }
            "*" -> {
                return "⭐"
            }
            "I" -> {
                return "💡"
            }
            "p" -> {
                return "👍"
            }
            "c" -> {
                return "👎"
            }
            "f" -> {
                return "🔥"
            }
            "k" -> {
                return "🔑"
            }
            "w" -> {
                return "🎂"
            }
            "u" -> {
                return "📈"
            }
            "d" -> {
                return "📉"
            }
        }

        return ""
    }

    private fun getEmojiForNumber(number: Int): String {
        when (number) {
            0 -> return "0️⃣"
            1 -> return "1️⃣"
            2 -> return "2️⃣"
            3 -> return "3️⃣"
            4 -> return "4️⃣"
            5 -> return "5️⃣"
            6 -> return "6️⃣"
            7 -> return "7️⃣"
            8 -> return "8️⃣"
            9 -> return "9️⃣"
        }
        return number.toString()
    }

    fun getTodoText(): String {

        val replaceLink = Regex("\\[([^]]+)]\\([^)]*\\)")

        return name.replace(replaceLink, "$1")

    }
}