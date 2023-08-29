package de.yukigasai.obsidianwidget

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.yukigasai.obsidianwidget.ui.theme.ObsidianWidgetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(!Environment.isExternalStorageManager()) {
            val i = Intent()
            i.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            startActivity(i)
        }

        setContent {
            ObsidianWidgetTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting()
                }
            }
        }
    }
}

@Composable
fun Greeting() {
    Text(
        text = "This is a widget app. This application only exists to allow the widget the Write External Storage Permission. Please grant the permission for the widget to work.",
    )
}