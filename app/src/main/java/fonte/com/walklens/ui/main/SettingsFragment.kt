package fonte.com.walklens.ui.main

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import fonte.com.walklens.R
import fonte.com.walklens.databinding.SettingsFragmentBinding
import fonte.com.walklens.util.*
import kotlinx.android.synthetic.main.settings_fragment.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class SettingsFragment : Fragment(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var vm: SettingsViewModel
    private var job: Job = Job()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val factory: SettingsViewModelFactory =
            InjectorUtils.provideSettingsFragmentViewModelFactory(requireContext())
        vm = ViewModelProviders.of(this, factory).get(SettingsViewModel::class.java)
        val binding: SettingsFragmentBinding = DataBindingUtil.inflate<SettingsFragmentBinding>(
            inflater,
            R.layout.settings_fragment,
            container,
            false
        ).apply {
            viewModel = vm
            lifecycleOwner = this@SettingsFragment
        }

        return binding.root
    }

    private fun initializeSettingsFragmentData() {
        vm.isLoading.value = true
        vm.getAllUserSettings().observe(this, Observer { userSettings ->
            if (vm.submitted) {
                return@Observer
            }
            if (userSettings == null) {
                return@Observer
            }
            vm.areNotificationsOn.value = userSettings.areNotificationsOn
            vm.isSoundOn.value = userSettings.isSoundOn
            vm.distanceFromCrosswalk.value = userSettings.distanceFromCrosswalk
            vm.frequencyOfNotifications.value = userSettings.frequencyOfNotifications
            vm.notificationStyle.value = userSettings.notificationStyle

            var notificationStyleIndex = 0
            when (userSettings.notificationStyle) {
                SELECTION_BANNER -> {
                    notificationStyleIndex = 0
                }
                SELECTION_LOCK_SCREEN -> {
                    notificationStyleIndex = 1
                }
                SELECTION_NOTIFICATION_CENTER -> {
                    notificationStyleIndex = 2
                }
            }
            notification_style_spinner.setSelection(notificationStyleIndex)
            vm.isLoading.value = false
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeSettingsFragmentData()

        settings_submit_button.setOnClickListener {
            vm.isLoading.value = true
            vm.submitted = false

            vm.getAllUserSettings().observe(this, Observer { userSettings ->
                if (vm.submitted) {
                    return@Observer
                }
                userSettings.areNotificationsOn = notification_toggle_switch.isChecked
                userSettings.isSoundOn = sound_switch.isChecked
                userSettings.distanceFromCrosswalk = distance_seekbar.progress
                userSettings.frequencyOfNotifications = notification_frequency_seekbar.progress
                userSettings.notificationStyle = notification_style_spinner.selectedItem.toString()

                vm.areNotificationsOn.value = userSettings.areNotificationsOn
                vm.isSoundOn.value = userSettings.isSoundOn
                vm.distanceFromCrosswalk.value = userSettings.distanceFromCrosswalk
                vm.frequencyOfNotifications.value = userSettings.frequencyOfNotifications
                vm.notificationStyle.value = userSettings.notificationStyle

                // launch thread to perform update
                launch {
                    vm.submitted = true
                    vm.updateUserSettings(userSettings)
                    vm.isLoading.value = false
                    Snackbar.make(view, "Settings successfully saved!", Snackbar.LENGTH_LONG).show()
                }
            })
        }

//        sample_notification_button.setOnClickListener {
//            // TODO: [DEBUG] Take this out for MVP when ready
//            generateNotification(
//                activity,
//                context,
//                getNotificationPriority(vm.isSoundOn.value, vm.notificationStyle.value)
//            )
//        }

        notification_time_start.setOnClickListener {
            showStartTimeDialog()
        }
        notification_time_end.setOnClickListener {
            showEndTimeDialog()
        }
    }


    private fun showStartTimeDialog() {
        val timePickerDialog = TimePickerDialog(
            context,
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minutes ->
                run {
                    vm.startHour = hourOfDay
                    vm.startMinute = minutes
                    var hourOutput: String = hourOfDay.toString()
                    var minuteOutput: String = minutes.toString()

                    if (hourOfDay > 12) {
                        hourOutput = (hourOfDay - 12).toString()
                    }
                    if (hourOfDay == 0) {
                        hourOutput = "12"
                    }
                    if (minutes < 10) { // single digit
                        minuteOutput = "0$minutes"
                    }
                    val suffix = if (hourOfDay >= 12) "p.m." else "a.m."
                    notification_time_start.setText("$hourOutput:$minuteOutput $suffix")
                }
            },
            12,
            0,
            false
        )
        timePickerDialog.show()
    }

    private fun showEndTimeDialog() {
        val timePickerDialog = TimePickerDialog(
            context,
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minutes ->
                run {
                    vm.endHour = hourOfDay
                    vm.endMinute = minutes
                    var hourOutput: String = hourOfDay.toString()
                    var minuteOutput: String = minutes.toString()

                    if (hourOfDay > 12) {
                        hourOutput = (hourOfDay - 12).toString()
                    }
                    if (hourOfDay == 0) {
                        hourOutput = "12"
                    }
                    if (minutes < 10) { // single digit
                        minuteOutput = "0$minutes"
                    }
                    val suffix = if (hourOfDay >= 12) "p.m." else "a.m."
                    notification_time_end.setText("$hourOutput:$minuteOutput $suffix")
                }
            },
            12,
            0,
            false
        )
        timePickerDialog.show()
    }


}