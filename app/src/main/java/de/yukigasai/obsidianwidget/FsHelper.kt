package de.yukigasai.obsidianwidget

import android.os.Environment
import java.io.File
import java.nio.charset.Charset


class FsHelper {
    fun getTodosFromFile(fileName: String): List<TodoItem>{
        val fileContent = loadTextData(fileName)
        return parseTodos(fileContent)
    }

    fun loadTextData(fileName: String): String {
        println(fileName)
        try {
            val file = File(
                Environment.getExternalStorageDirectory()
                    .absolutePath, fileName
            )
            if (!file.exists()) return ""
            return file.readText(Charset.defaultCharset())
        }catch(err: Exception) {
            println(err)
            return ""
        }
    }

    fun parseTodos(fileContent: String): List<TodoItem>{
        return Regex("(\\s*)- \\[([ x])] (.*)\$", RegexOption.MULTILINE)
            .findAll(fileContent)
            .mapIndexed  {
                    index, todo ->  TodoItem(index, todo.groupValues[3], todo.groupValues[2] != " ", todo.groupValues[1])
            }.toList()
    }

    fun overwriteFile(fileName: String, content: String) {
        val file = File(
            Environment.getExternalStorageDirectory()
                .absolutePath, fileName
        )
        file.writeText(content, Charset.defaultCharset())
    }

    fun updateTaskInFile(fileName: String, item: TodoItem) {
        val originalFile = FsHelper().loadTextData(fileName)
        val newFileData: String
        if(item.isChecked){
            newFileData = originalFile.replace("- [x] ${item.name}", "- [ ] ${item.name}")
        }else {
            newFileData = originalFile.replace("- [ ] ${item.name}", "- [x] ${item.name}")
        }
        overwriteFile(fileName, newFileData)
    }

}