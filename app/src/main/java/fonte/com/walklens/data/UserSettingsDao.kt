package fonte.com.walklens.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings LIMIT 1")
    fun getAllUserSettings(): LiveData<UserSettings>

    @Update
    suspend fun updateUserSettings(userSettings: UserSettings)

    @Insert
    suspend fun insertUserSettings(userSettings: UserSettings)
}