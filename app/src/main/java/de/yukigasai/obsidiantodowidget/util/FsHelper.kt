package de.yukigasai.obsidiantodowidget.util

import android.os.Environment
import de.yukigasai.obsidiantodowidget.WidgetConfig
import java.io.File
import java.nio.charset.Charset

object FsHelper {
        fun loadTextData(config: WidgetConfig): String {
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
       fun overwriteFile(fileName: String, content: String) {
            val file = File(
                Environment.getExternalStorageDirectory()
                    .absolutePath, fileName
            )
            file.writeText(content, Charset.defaultCharset())
        }
    }
