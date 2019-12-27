package fonte.com.walklens.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import fonte.com.walklens.MainActivity
import fonte.com.walklens.R


class LocationService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForeground() {

        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(
                    "background_location_service",
                    "Background Location Service"
                )
            } else {
                ""
            }

        val notificationIntent = Intent(this, MainActivity::class.java)

        val deleteIntent = Intent(this, DeleteLocationService::class.java)

        val deletePendingIntent =
            PendingIntent.getService(this, 278, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            notificationIntent, 0
        )

        startForeground(
            NOTIF_ID, NotificationCompat.Builder(
                this,
                channelId
            ) // don't forget create a notification channel first
                .setOngoing(true)
                .setSmallIcon(R.drawable.walklens_logo_cropped)
                .setContentTitle("WalkLens is running in the background.")
                .setContentText("Periodically checking if you are close to a crosswalk.")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .addAction(android.R.drawable.arrow_up_float, "Open App", pendingIntent)
                .addAction(android.R.drawable.ic_delete, "Turn Off", deletePendingIntent)
                .build()
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    companion object {
        private const val NOTIF_ID = 1
    }


}