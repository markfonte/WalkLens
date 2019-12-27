package fonte.com.walklens.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class DeleteLocationService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        stopService(Intent(this, LocationService::class.java))
        return super.onStartCommand(intent, flags, startId)
    }
}