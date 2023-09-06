package de.yukigasai.obsidiantodowidget

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.gson.Gson
class MyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val gson = Gson()
        if (intent.action != Constants.Actions.START_NOTIFICATION) {
            System.err.println("OK no action fits here")
            return
        }

        val config = ListSharedPrefsUtil.loadWidgetSettings(context)

        val title = intent.getStringExtra(Constants.Extras.NOTIFICATION_TITLE)
        val desc = intent.getStringExtra(Constants.Extras.NOTIFICATION_MESSAGE)
        val todo = gson.fromJson(intent.getStringExtra(Constants.Extras.NOTIFICATION_TODO),  TodoItem::class.java)
        println("OKay I should send the notification for ${todo.name}")

        // CREATE CALLBACK FOR CHECK OPTION
        val checkIntent = Intent(context, TodoWidgetReceiver::class.java)
        checkIntent.action = Constants.Actions.END_NOTIFICATION_CHECK
        checkIntent.putExtra(Constants.Extras.NOTIFICATION_TODO, gson.toJson(todo))

        val checkPendingIntent = PendingIntent.getBroadcast(context, 0, checkIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // CREATE CALLBACK FOR OPEN OPTION
        val openIntent = Intent(Intent.ACTION_VIEW, Uri.parse(config.getFullObsidianURL())).apply {
            flags = FLAG_ACTIVITY_NEW_TASK
        }

        // Create the TaskStackBuilder
        val openPendingIntent: PendingIntent? = TaskStackBuilder.create(context
        ).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(openIntent)
            // Get the PendingIntent containing the entire back stack
            NotificationManagerCompat.from(context).cancel(null, todo.name.hashCode())
            getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        // Create a notification channel needed for Android O and above
        val channelId = Constants.Channel.ID
        val channelName = Constants.Channel.NAME
        val importance = NotificationManager.IMPORTANCE_HIGH
        val notificationChannel = NotificationChannel(channelId, channelName, importance)
        // Get the notification manager from the system
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Create the notification builder and set the properties
        val builder = NotificationCompat.Builder(context, channelId)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setBadgeIconType(R.mipmap.ic_launcher)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(desc)
            .addAction( NotificationCompat.Action.Builder(R.drawable.file_open, "Open", openPendingIntent).build())
            .addAction(NotificationCompat.Action.Builder(R.drawable.check, "Done", checkPendingIntent).build())
            .setAutoCancel(true)
        // Register the channel with the notification manager
        notificationManager.createNotificationChannel(notificationChannel)
        // Send the notification with a unique id
        notificationManager.notify(todo.name.hashCode(), builder.build())
    }
}