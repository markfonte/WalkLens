package fonte.com.walklens.util

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import fonte.com.walklens.MainActivity
import fonte.com.walklens.R

/**
 * Somewhat arbitrary changes to type of notification can reduce the priority of the WalkLens notifications
 */
fun getNotificationPriority(isSoundOn: Boolean?, notificationStyle: String?): Int {
    if (isSoundOn == null || notificationStyle == null) {
        return NotificationCompat.PRIORITY_DEFAULT
    }
    return if (!isSoundOn) {
        NotificationCompat.PRIORITY_MIN
    } else if (notificationStyle == SELECTION_NOTIFICATION_CENTER) {
        NotificationCompat.PRIORITY_LOW
    } else if (isSoundOn && notificationStyle == SELECTION_BANNER) {
        NotificationCompat.PRIORITY_MAX
    } else if (isSoundOn && notificationStyle == SELECTION_LOCK_SCREEN) {
        NotificationCompat.PRIORITY_HIGH
    } else {
        NotificationCompat.PRIORITY_DEFAULT
    }
}

fun generateNotification(activity: Activity?, context: Context?, priority: Int?) {
    if (activity == null || context == null) {
        return
    }
    // Watch out - approaching crosswalk
    val CHANNEL_ID = "128"
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Channel name"
        val descriptionText = "Channel description"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(channel)
    }

    val notificationIntent = Intent(context, MainActivity::class.java)

    val pendingIntent = PendingIntent.getActivity(
        context, 0,
        notificationIntent, 0
    )

    val notifPriority = priority ?: NotificationCompat.PRIORITY_DEFAULT
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_warning_white_24dp)
        .setContentTitle("Watch Out!")
        .setContentText("Approaching Crosswalk")
        .setPriority(notifPriority)
        .setAutoCancel(true)
        .addAction(android.R.drawable.arrow_up_float, "Open App", pendingIntent)

    with(NotificationManagerCompat.from(context)) {
        notify(0, builder.build())
    }
}