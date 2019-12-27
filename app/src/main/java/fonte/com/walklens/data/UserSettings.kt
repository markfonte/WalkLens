package fonte.com.walklens.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "user_settings",
    indices = [Index("user_id")]
)
data class UserSettings(

    /**
     *  Should only be one user on the device, so always index
     *  in with user_id == 0
     */
    @ColumnInfo(name = "user_id") val userId: Int,

    /**
     *  Display name is the name a user enters
     */
    @ColumnInfo(name = "display_name") var displayName: String,

    /**
     *  True if sound is on
     *  False if sound is off
     */
    @ColumnInfo(name = "is_sound_on") var isSoundOn: Boolean,

    /**
     *  True if notifications are on
     *  False if notifications are off
     */
    @ColumnInfo(name = "are_notifications_on") var areNotificationsOn: Boolean,

    /**
     *  Scale from 0 to 4 of notification distance from crosswalk
     *  The real world translation of this can be found empirically later
     *  1 is closer to crosswalk
     *  5 is further from crosswalk
     */
    @ColumnInfo(name = "distance_from_crosswalk") var distanceFromCrosswalk: Int,

    /**
     * Scale from 0 to 4 of notification frequency
     * The real world translation of this can be found empirically later
     * 1 is less frequent
     * 5 is more frequent
     */
    @ColumnInfo(name = "frequency_of_notifications") var frequencyOfNotifications: Int,


    /**
     * One of the following, representing notification style:
     * const val SELECTION_BANNER = "Banner"
     * const val SELECTION_LOCK_SCREEN = "Lock Screen"
     * const val SELECTION_NOTIFICATION_CENTER = "Notification Center"
     */
    @ColumnInfo(name = "notification_style") var notificationStyle: String
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var userSettingsId: Long = 0
}