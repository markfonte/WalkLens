package fonte.com.walklens.ui.main

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import fonte.com.walklens.MainActivity
import fonte.com.walklens.R
import fonte.com.walklens.databinding.MapsFragmentBinding
import fonte.com.walklens.util.InjectorUtils

class MapsFragment : Fragment(), OnMapReadyCallback {
    private lateinit var vm: MapsViewModel
    private lateinit var mMap: GoogleMap
    private var fusedLocationClient: FusedLocationProviderClient? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val factory: MapsViewModelFactory =
            InjectorUtils.provideMapsFragmentViewModelFactory(requireContext())
        vm = ViewModelProviders.of(this, factory).get(MapsViewModel::class.java)
        val binding: MapsFragmentBinding = DataBindingUtil.inflate<MapsFragmentBinding>(
            inflater,
            R.layout.maps_fragment,
            container,
            false
        ).apply {
            viewModel = vm
            lifecycleOwner = this@MapsFragment
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (childFragmentManager.fragments[0] as SupportMapFragment).getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ContextCompat.checkSelfPermission(
                context!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Disclaimer
            val dialogBuilder = AlertDialog.Builder(activity!!)

            dialogBuilder.setNeutralButton("I understand", null)

            val alert = dialogBuilder.create()
            alert.setTitle("WalkLens Disclaimer")

            alert.setMessage(
                "This application is for educational purposes." +
                        "We are not held accountable for any injuries that result from using our application."
            )
            alert.show()

            // Settings pane
            val dialogBuilderSettings = AlertDialog.Builder(activity!!)

            dialogBuilderSettings.setNeutralButton("I understand", null)

            val alertSettings = dialogBuilderSettings.create()
            alertSettings.setTitle("Adjust Settings")

            alertSettings.setMessage(
                "Please navigate to settings pane on top right corner to customize notifications."
            )
            alertSettings.show()

            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity!!,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            }
        } else {
            locationAccessGranted()
        }
        if (ContextCompat.checkSelfPermission(
                context!!,
                Manifest.permission.FOREGROUND_SERVICE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity!!,
                    Manifest.permission.FOREGROUND_SERVICE
                )
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    requestPermissions(
                        arrayOf(Manifest.permission.FOREGROUND_SERVICE),
                        2
                    )
                }

            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    requestPermissions(
                        arrayOf(Manifest.permission.FOREGROUND_SERVICE),
                        2
                    )
                }
            }
        } else {
            // access granted
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    locationAccessGranted()
                } else {
                    Log.d(LOG_TAG, "Location Permission Denied! Exiting app.")
                    activity?.finish()
                }
                return
            }
            2 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    (activity as MainActivity).startForegroundService()
                }
            }
            else -> {
                Log.d(LOG_TAG, "Permission Code $requestCode provided. Please investigate.")
            }
        }
    }

    private fun locationAccessGranted() {
        (activity as? MainActivity)?.allowLocationAccess(true)
        mMap.isMyLocationEnabled = true
        moveToCurLocation()
    }

    private fun moveToCurLocation() {
        fusedLocationClient?.lastLocation?.addOnSuccessListener { lastKnownLocation: Location? ->
            val curLocation: LatLng = lastKnownLocation?.latitude?.let { latitude ->
                LatLng(latitude, lastKnownLocation.longitude)
            }
                ?: return@addOnSuccessListener
            // zoom level 14.6f is appropriate
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curLocation, 14.6f))
        }
    }


    companion object {
        private val LOG_TAG = MapsFragment::class.java.name

    }
}
