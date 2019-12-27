package fonte.com.walklens.data

import androidx.lifecycle.LiveData

class MainRepository private constructor(
    private val userSettingsDao: UserSettingsDao
) {

    fun getUserAllSettings(): LiveData<UserSettings> = userSettingsDao.getAllUserSettings()

    suspend fun updateUserSettings(userSettings: UserSettings) = userSettingsDao.updateUserSettings(userSettings)

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: MainRepository? = null

        fun getInstance(userSettingsDao: UserSettingsDao) =
            instance ?: synchronized(this) {
                instance ?: MainRepository(userSettingsDao).also { instance = it }
            }
    }
}