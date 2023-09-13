package de.yukigasai.obsidiantodowidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import de.yukigasai.obsidiantodowidget.databinding.ActivityWidgetConfigureBinding
import de.yukigasai.obsidiantodowidget.util.ActionsConstants
import de.yukigasai.obsidiantodowidget.util.WidgetLogger

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

        val config = WidgetConfig.loadFromPrefs(this)

        binding.etConfigVaultName.setText(config.vaultName)
        binding.etConfigFolderPath.setText(config.folder)
        binding.etConfigFileName.setText(config.fileName)
        binding.switchConfigHideDone.isChecked = config.hideDoneTasks
        binding.etConfigHeader.setText(config.header)
        binding.switchConfigIncludeSubHeader.isClickable = binding.etConfigHeader.text.isNotBlank()
        binding.switchConfigIncludeSubHeader.isActivated = binding.etConfigHeader.text.isNotBlank()
        binding.switchConfigIncludeSubHeader.isEnabled = binding.etConfigHeader.text.isNotBlank()
        binding.switchConfigIncludeSubHeader.isChecked = config.includeSubHeader



        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val url = "/${data?.data?.path?.split(":")?.last()?.replace("/storage/emulated/0/","")}/"
                binding.etConfigFolderPath.setText(url)
            } else {
                WidgetLogger.warn("Select Folder Intent resulted in ${result.resultCode}")
            }
        }

        binding.btnConfigSelectFolder.setOnClickListener {
            val i = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            val intent = Intent.createChooser(i, "Choose folder")
            resultLauncher.launch(intent)
        }

        binding.etConfigHeader.addTextChangedListener (object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                binding.switchConfigIncludeSubHeader.isClickable = s.isNotBlank()
                binding.switchConfigIncludeSubHeader.isActivated = s.isNotBlank()
                binding.switchConfigIncludeSubHeader.isEnabled = s.isNotBlank()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        binding.btnConfigCreate.setOnClickListener {
            config.vaultName = binding.etConfigVaultName.text.toString()
            config.folder = binding.etConfigFolderPath.text.toString()
            config.fileName = binding.etConfigFileName.text.toString()
            config.hideDoneTasks = binding.switchConfigHideDone.isChecked
            config.header = binding.etConfigHeader.text.toString()
            config.includeSubHeader = binding.switchConfigIncludeSubHeader.isChecked
            onWidgetContainerClicked(config)
        }

    }

    private fun onWidgetContainerClicked(widgetConfig: WidgetConfig) {
        widgetConfig.saveToPrefs(this)
        // It is the responsibility of the configuration activity to update the app widget

        // Make sure we pass back the original appWidgetId.
        val resultData = Intent(this, TodoWidgetReceiver::class.java)
        resultData.action = ActionsConstants.UPDATE_WIDGET
        resultData.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

        setResult(RESULT_OK, resultData)

        sendBroadcast(Intent(resultData))
        finish()
    }
}