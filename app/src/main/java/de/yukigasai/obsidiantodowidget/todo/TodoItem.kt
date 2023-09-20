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
    fun getTodoText(): String {

        if(this.state.lowercase() == "x") {
            return this.name
        }

        if(this.repeats != null) {
            return "${getEmojiForNumber(this.count)} ${this.name}"
        }

        when (this.state) {
            "/" -> {
                return "🔃 ${this.name}"
            }
            "-" -> {
                return "✖️ ${this.name}"
            }
            ">" -> {
                return "➡️ ${this.name}"
            }
            "<" -> {
                return "📅 ${this.name}"
            }
            "?" -> {
                return "❓ ${this.name}"
            }
            "!" -> {
                return "❗ ${this.name}"
            }
            "\"" -> {
                return "💬 ${this.name}"
            }
            "l" -> {
                return "📍 ${this.name}"
            }
            "b" -> {
                return "🔖 ${this.name}"
            }
            "i" -> {
                return "ℹ️ ${this.name}"
            }
            "s" -> {
                return "💲 ${this.name}"
            }
            "I" -> {
                return "💡 ${this.name}"
            }
            "p" -> {
                return "👍 ${this.name}"
            }
            "c" -> {
                return "👎 ${this.name}"
            }
            "f" -> {
                return "🔥 ${this.name}"
            }
            "k" -> {
                return "🔑 ${this.name}"
            }
            "w" -> {
                return "🎂 ${this.name}"
            }
            "u" -> {
                return "📈 ${this.name}"
            }
            "d" -> {
                return "📉 ${this.name}"
            }
        }

        return this.name
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

}