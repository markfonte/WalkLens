package fonte.com.walklens.data

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import fonte.com.walklens.util.SELECTION_BANNER
import kotlinx.coroutines.coroutineScope

class SeedDatabaseWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = coroutineScope {
        try {
            val database = AppDatabase.getInstance(applicationContext)
            val tempUserSettings = UserSettings(
                0,
                "",
                isSoundOn = true,
                areNotificationsOn = true,
                distanceFromCrosswalk = 2,
                frequencyOfNotifications = 2,
                notificationStyle = SELECTION_BANNER
            )
            database.userSettingsDao().insertUserSettings(tempUserSettings)
            Result.success()
        } catch (ex: Exception) {
            Log.e(TAG, "Error seeding database", ex)
            Result.failure()
        }
    }

    companion object {
        private val TAG = SeedDatabaseWorker::class.java.simpleName
    }
}