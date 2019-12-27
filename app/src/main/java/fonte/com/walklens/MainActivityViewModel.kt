package fonte.com.walklens

import androidx.lifecycle.ViewModel
import fonte.com.walklens.data.MainRepository
import fonte.com.walklens.util.SELECTION_BANNER

class MainActivityViewModel(private val mainRepository: MainRepository) : ViewModel() {
    fun getAllUserSettings() = mainRepository.getUserAllSettings()

    var lastKnownLatitude: Double = 0.0
    var lastKnownLongitude: Double = 0.0
    var secondToLastKnownLatitude: Double = 0.0
    var secondToLastKnownLongitude: Double = 0.0
    var notificationInterval: Long = 0
    var currentSpeed: Double = 0.0 // in feet per second
    var currentDirection: Double = 0.0 // counter-clockwise degrees from 0

    var haveSettingsBeenPulled =
        false // false until settings are pulled from database for the first time

    var areNotificationsOn: Boolean = false
    var distanceFromCrosswalk: Int = 0
    var frequencyOfNotifications: Int = 0
    var isSoundOn: Boolean = false
    var notificationStyle: String = SELECTION_BANNER
}