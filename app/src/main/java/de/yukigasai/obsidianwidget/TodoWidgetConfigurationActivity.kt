package de.yukigasai.obsidianwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import de.yukigasai.obsidianwidget.databinding.ActivityWidgetConfigureBinding

class TodoWidgetConfigurationActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // Find the widget id from the intent.
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // If this activity was started with an intent without an app widget ID, just finish.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        // Make sure we pass back the original appWidgetId.
        val resultData = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_CANCELED, resultData)

        val binding = ActivityWidgetConfigureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val config = ListSharedPrefsUtil.loadWidgetSettings(this)

        binding.switchUseRegex.isChecked = config.useRegex
        binding.eTVaultName.setText(config.vaultName)
        binding.etFolderPath.setText(config.folder)
        binding.etFileName.setText(config.fileName)
        binding.switchHideDone.isChecked = config.hideDoneTasks

        binding.switchUseRegex.setOnCheckedChangeListener { button, isChecked ->
            config.useRegex = isChecked
        }

        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val url = "/${data?.data?.path?.split(":")?.last()?.replace("/storage/emulated/0/","")}/"
                binding.etFolderPath.setText(url)
            }
        }

        binding.btnSelectFolder.setOnClickListener {
            val i = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            val intent = Intent.createChooser(i, "Choose folder")
            resultLauncher.launch(intent)
        }

        binding.btnCreateWidget.setOnClickListener {
            config.vaultName = binding.eTVaultName.text.toString()
            config.folder = binding.etFolderPath.text.toString()
            config.fileName = binding.etFileName.text.toString()
            config.hideDoneTasks = binding.switchHideDone.isChecked
            onWidgetContainerClicked(config)
        }

    }

    private fun onWidgetContainerClicked(widgetConfig: WidgetConfig) {
        ListSharedPrefsUtil.saveWidgetSettings(this, widgetConfig)
        // It is the responsibility of the configuration activity to update the app widget


        // Make sure we pass back the original appWidgetId.
        val resultData = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultData)

        val i = Intent("de.yukigasai.obsidianwidget.UPDATE_CONFIG_ACTION")
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        sendBroadcast(i)
        finish()
    }
}