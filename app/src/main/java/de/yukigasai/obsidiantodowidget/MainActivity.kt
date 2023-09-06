package de.yukigasai.obsidiantodowidget

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import de.yukigasai.obsidiantodowidget.ui.theme.ObsidianTodoWidgetTheme


class MainActivity : ComponentActivity() {

    private val STORAGE_PERMISSION_CODE = 23

    private fun requestForStoragePermissions() {
        //Android is 11 (R) or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val i = Intent()
            i.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            startActivity(i)
        } else {
            //Below android 11
            ActivityCompat.requestPermissions(
                this, arrayOf<String>(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                STORAGE_PERMISSION_CODE
            )
        }
    }

    private fun checkForNotificationPermission(): Boolean{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
             return (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
        }
        return true
    }

    fun checkStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Android is 11 (R) or above
            Environment.isExternalStorageManager()
        } else {
            //Below android 11
            val write =
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read =
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(!checkStoragePermissions()){
            requestForStoragePermissions()
        }

        if(!checkForNotificationPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        setContent {
            ObsidianTodoWidgetTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting()
//                    ColorBox()
                }
            }
        }
    }
}

@Composable
fun Greeting() {
    Text(
        modifier = Modifier.padding(8.dp),
        text = "",
    )
    Text(
        modifier = Modifier.padding(8.dp),
        text = "This is a widget app. This application only exists to allow the widget the Write External Storage Permission. Please grant the permission for the widget to work.",
    )
}
@Composable
fun ColorBox() {
    Column(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1F)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Text(text = "Primary", color = MaterialTheme.colorScheme.onPrimary)
        }
        Box(
            modifier = Modifier
                .weight(1F)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text(text = "PrimaryContainer", color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Box(
            modifier = Modifier
                .weight(1F)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondary)
        ) {
            Text(text = "Secondary", color = MaterialTheme.colorScheme.onSecondary)
        }
        Box(
            modifier = Modifier
                .weight(1F)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text(text = "SecondaryContainer", color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
        Box(
            modifier = Modifier
                .weight(1F)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.tertiary)
        ) {
            Text(text = "Tenary", color = MaterialTheme.colorScheme.onTertiary)
        }
        Box(
            modifier = Modifier
                .weight(1F)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Text(text = "TenaryContainer", color = MaterialTheme.colorScheme.onTertiaryContainer)
        }
        Box(
            modifier = Modifier
                .weight(1F)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Text(text = "Background", color = MaterialTheme.colorScheme.onBackground)
        }
        Box(
            modifier = Modifier
                .weight(1F)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Text(text = "Surface", color = MaterialTheme.colorScheme.onSurface)
        }
        Box(
            modifier = Modifier
                .weight(1F)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(text = "SurfaceVariant", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview(name = "DayView")
@Composable
fun ColorBoxLight() {
    ObsidianTodoWidgetTheme(darkTheme = false) {
        ColorBox()
    }
}

@Preview(name = "NightView", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun ColorBoxDark() {
    ObsidianTodoWidgetTheme(darkTheme = true) {
        ColorBox()
    }
}