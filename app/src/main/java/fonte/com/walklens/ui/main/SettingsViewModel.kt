package fonte.com.walklens.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fonte.com.walklens.data.MainRepository
import fonte.com.walklens.data.UserSettings
import fonte.com.walklens.util.SELECTION_BANNER

class SettingsViewModel(private val mainRepository: MainRepository) : ViewModel() {

    val isLoading: MutableLiveData<Boolean> = MutableLiveData()
    var submitted: Boolean = false
    val isSoundOn: MutableLiveData<Boolean> = MutableLiveData()
    val areNotificationsOn: MutableLiveData<Boolean> = MutableLiveData()
    val distanceFromCrosswalk: MutableLiveData<Int> = MutableLiveData()
    val frequencyOfNotifications: MutableLiveData<Int> = MutableLiveData()
    var notificationStyle: MutableLiveData<String> = MutableLiveData()
    var startHour: Int = 0
    var startMinute: Int = 0
    var endHour: Int = 0
    var endMinute: Int = 0

    init {
        isLoading.value = false
    }
    fun getAllUserSettings() : LiveData<UserSettings> = mainRepository.getUserAllSettings()


    suspend fun updateUserSettings(userSettings: UserSettings) {
        mainRepository.updateUserSettings(userSettings)
    }
}