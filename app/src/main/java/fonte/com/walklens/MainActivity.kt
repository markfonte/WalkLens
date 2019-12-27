package fonte.com.walklens

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import fonte.com.walklens.databinding.MainActivityBinding
import fonte.com.walklens.service.LocationService
import fonte.com.walklens.util.InjectorUtils
import fonte.com.walklens.util.generateNotification
import fonte.com.walklens.util.getNotificationPriority
import kotlinx.android.synthetic.main.main_activity.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {


    private lateinit var vm: MainActivityViewModel
    lateinit var navController: NavController
    private lateinit var locationCallback: LocationCallback
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequestCreated = false
    private var isDeadReckoningHappening = false

    fun allowLocationAccess(allowed: Boolean) {
        if (allowed && !locationRequestCreated) {
            if (!vm.haveSettingsBeenPulled || !vm.areNotificationsOn) { // don't send notifications while user settings haven't been pulled
                vm.getAllUserSettings().observe(this, Observer {
                    if (it == null) {
                        return@Observer
                    }

                    vm.distanceFromCrosswalk = it.distanceFromCrosswalk
                    vm.frequencyOfNotifications = it.frequencyOfNotifications
                    vm.areNotificationsOn = it.areNotificationsOn
                    vm.isSoundOn = it.isSoundOn
                    vm.notificationStyle = it.notificationStyle

                    vm.haveSettingsBeenPulled = true

                    if (!locationRequestCreated) {
                        createLocationRequest()
                    }
                })
            } else {
                createLocationRequest()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_MaterialComponents_Light_DarkActionBar)
        val factory: MainActivityViewModelFactory =
            InjectorUtils.provideMainActivityViewModelFactory(this)
        vm = ViewModelProviders.of(this, factory).get(MainActivityViewModel::class.java)
        DataBindingUtil.setContentView<MainActivityBinding>(this, R.layout.main_activity)
            .apply {
                viewModel = vm
                lifecycleOwner = this@MainActivity
            }

        val navHost: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment?
                ?: return

        navController = navHost.navController
        NavigationUI.setupActionBarWithNavController(this, navController)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = buildLocationCallback()
        vm.getAllUserSettings().observe(this, Observer {
            if (it == null) {
                return@Observer
            }
            vm.distanceFromCrosswalk = it.distanceFromCrosswalk
            vm.frequencyOfNotifications = it.frequencyOfNotifications
            vm.areNotificationsOn = it.areNotificationsOn
            vm.isSoundOn = it.isSoundOn
            vm.notificationStyle = it.notificationStyle

            vm.haveSettingsBeenPulled = true
        })
        startForegroundService()
    }

    fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.FOREGROUND_SERVICE
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                startService(Intent(this, LocationService::class.java))
            } else {
                Log.d(LOG_TAG, "Foreground service permission not granted.")
            }
        } else {
            startService(Intent(this, LocationService::class.java))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            navController.navigate(
                R.id.action_mapsFragment_to_settingsFragment,
                null
            )
            return true
        }
        return false
    }


    inner class CallbackImpl : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e(LOG_TAG, "$call, $e")
        }

        override fun onResponse(call: Call, response: Response) {
            try {
                if (isDeadReckoningHappening) {
                    return
                }
                val responseData: String? = response.body?.string()
                if (responseData == null) {
                    Log.e(LOG_TAG, "Response is null")
                    return
                }
                val responseJson = JSONObject(responseData)
                val snappedPoints: JSONArray = responseJson.get("snappedPoints") as JSONArray
                val distances: ArrayList<Double> = arrayListOf()
                var directionToNearestRoad = 0.0
                for (i in 0 until snappedPoints.length()) {
                    if (i == 1) {
                        break
                    }
                    val road: JSONObject = snappedPoints.get(i) as JSONObject
                    val roadLatLng: JSONObject = road.get("location") as JSONObject
                    val roadLat: Double = roadLatLng.get("latitude") as Double
                    val roadLng: Double = roadLatLng.get("longitude") as Double
                    directionToNearestRoad = determineDirection(
                        vm.lastKnownLatitude,
                        vm.lastKnownLongitude, roadLat,
                        roadLng
                    )
                    distances.add(
                        haversine(
                            roadLat,
                            roadLng,
                            vm.lastKnownLatitude,
                            vm.lastKnownLongitude
                        )
                    )
                }
                if (distances.isNotEmpty()) {
                    sendNotificationIfClose(distances[0], directionToNearestRoad)
                }


            } catch (e: JSONException) {
                Log.e(LOG_TAG, "JSON exception parsing response")
            }
        }
    }


    private fun buildLocationCallback(): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return

                var exit = false
                for (location in locationResult.locations) {
                    if (exit) {
                        break
                    }
                    Log.d(LOG_TAG, "Lat: ${location.latitude}, Lng: ${location.longitude}")
                    val client = OkHttpClient()

                    val url =
                        "https://roads.googleapis.com/v1/nearestRoads?points=${location.latitude},${location.longitude}&key=" + getString(
                            R.string.google_maps_key
                        )
                    val request: Request? = Request.Builder()
                        .url(url)
                        .build()
                    client.newCall(request!!).enqueue(CallbackImpl())
                    exit = true
                    vm.secondToLastKnownLatitude = vm.lastKnownLatitude
                    vm.secondToLastKnownLongitude = vm.lastKnownLongitude
                    vm.lastKnownLatitude = location.latitude
                    vm.lastKnownLongitude = location.longitude
                    if (vm.secondToLastKnownLatitude == 0.0) { // not enough data points yet
                        break
                    }
                    vm.currentSpeed = haversine( //distance
                        vm.secondToLastKnownLatitude,
                        vm.secondToLastKnownLongitude,
                        vm.lastKnownLatitude,
                        vm.lastKnownLongitude
                    ) / (vm.notificationInterval / 1000) //divided by time in milliseconds

                    vm.currentDirection = determineDirection(
                        vm.secondToLastKnownLatitude,
                        vm.secondToLastKnownLongitude,
                        vm.lastKnownLatitude,
                        vm.lastKnownLongitude
                    )
                }
            }
        }
    }

    private fun createLocationRequest() {
        locationRequestCreated = true
        // tries every 25-30 for the least frequent and 5-10 seconds for the most frequent

        vm.notificationInterval = (30000 - (vm.frequencyOfNotifications) * 5000).toLong()
        val locationRequest = LocationRequest.create().apply {
            interval = vm.notificationInterval // in milliseconds
            fastestInterval = vm.notificationInterval // in milliseconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { locationSettingsResponse ->
            Log.d(LOG_TAG, locationSettingsResponse.toString())
            fusedLocationClient?.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

        }
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(
                        this,
                        3
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                }
            }

            Log.e(LOG_TAG, "Check location task failed $exception. This should not happen.")
        }
    }


    /**
     * All units in this function are in feet.
     */
    private fun sendNotificationIfClose(
        distanceToNearestRoad: Double,
        directionTowardsNearestRoad: Double
    ) {
        if (!vm.haveSettingsBeenPulled || !vm.areNotificationsOn) { // don't send notifications while user settings haven't been pulled
            return
        }

        // empirically determined. distance is from center of road, so a buffer of 20 feet is a good starting point
        var adjustedDistance: Double = distanceToNearestRoad - 20.0
        if (adjustedDistance < 0.0) {
            adjustedDistance = 0.0
        }
        // vm.distanceFromCrosswalk is Int 0-4 so we are looking to send a notification between 20 + 10*distanceFromCrosswalk
        // or effectively 20-60 feet away from center of nearest road
        if (adjustedDistance < 10.0 * vm.distanceFromCrosswalk) {
            // approaching crosswalk. send notification

//            // part of DEAD RECKONING implementation
//            // if user is currently not walking towards the crosswalk, don't generate notification
//            if (getCardinalDirection(curDirection) != getCardinalDirection(vm.currentDirection)) {
//                return
//            }

            // else, they are walking towards crosswalk
            sendNotification()
        } else {
            val delay =
                willBeApproachingCrosswalk(distanceToNearestRoad, directionTowardsNearestRoad)
            if (delay > 0) {
                val handler = Handler(Looper.getMainLooper())

                val r = Runnable {
                    isDeadReckoningHappening = false
                    generateNotification(
                        this,
                        this,
                        getNotificationPriority(vm.isSoundOn, vm.notificationStyle)
                    )
                }
                isDeadReckoningHappening = true
                handler.postDelayed(r, delay)
            }
        }
    }

    private fun sendNotification() {
        isDeadReckoningHappening = false
        generateNotification(
            this,
            this,
            getNotificationPriority(vm.isSoundOn, vm.notificationStyle)
        )
    }

    /**
     * Dead reckoning implementation.
     * Uses user speed and direction to predict if they will be approaching a crosswalk soon.
     * If they will be, returns the approximate time in which they should be approaching the crosswalk.
     * Else, returns 0
     */
    private fun willBeApproachingCrosswalk(
        distanceToNearestRoad: Double,
        directionTowardsNearestRoad: Double
    ): Long {
        var result: Long = 0

        if (distanceToNearestRoad > 1000) {
            return result
        }
        // if you're going in the same direction as the direction from you to the nearest road
        if (getCardinalDirection(vm.currentDirection) == getCardinalDirection(
                directionTowardsNearestRoad
            )
        ) {
            result = (distanceToNearestRoad / vm.currentSpeed).toLong()

        }
        return result
    }


    companion object {

        private val LOG_TAG = MainActivity::class.java.name

        /**
         * Returns distance in feet between two points
         */
        fun haversine(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
            val r = 6371.0 // average radius of the earth in km
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lng2 - lng1)
            val a: Double = sin(dLat / 2.0) * sin(dLat / 2.0) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2.0) * sin(
                dLon / 2.0
            )
            val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
            return r * c * 1000.0 * 3.280839895
        }

        /**
         * Returns counter-clockwise degrees from 0
         */
        fun determineDirection(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
            val longDiff: Double = lng2 - lng1
            val y = Math.sin(longDiff) * Math.cos(lat2)
            val x = (Math.cos(lat1) * Math.sin(lat2)) - (Math.sin(lat1) * Math.cos(lat2) * Math.cos(
                longDiff
            ))
            return 360 - ((Math.toDegrees(Math.atan2(y, x)) + 360) % 360)
        }

        /**
         * Returns 'N', 'S', 'E', or 'W' corresponding to direction degree value is pointing to
         */
        fun getCardinalDirection(degrees: Double): Char {
            return if (degrees in 315.0..360.0 || degrees in 0.0..45.0) {
                'E'
            } else if (degrees in 45.0..135.0) {
                'N'
            } else if (degrees in 135.0..225.0) {
                'W'
            } else if (degrees in 225.0..315.0) {
                'S'
            } else {
                // fail silently
                Log.e(LOG_TAG, "error computing cardinal direction, this should not happen.")
                'N'
            }
        }
    }
}
