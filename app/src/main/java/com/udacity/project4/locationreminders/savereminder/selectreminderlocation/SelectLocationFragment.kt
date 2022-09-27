package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import org.koin.android.ext.android.inject
import java.lang.Exception
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    private val TAG = "SelectLocationFragment"
    private lateinit var mMap: GoogleMap
    private lateinit var pPoi: PointOfInterest
    private val REQUEST_LOCATION_PERMISSION = 1
    private val REQUEST_TURN_DEVICE_LOCATION_ON = 29

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var selectLocationMarker: Marker

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
//        setDisplayHomeAsUpEnabled(true)

        setupSelectLocationFragment()

        binding.saveLocationBtn.setOnClickListener {
            onLocationSelected()
        }
        return binding.root
    }

    private fun setupSelectLocationFragment() {
        val supportMapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment?
        supportMapFragment?.getMapAsync(this)
    }

    private fun onLocationSelected() {
        when {
            this::selectLocationMarker.isInitialized -> {
                _viewModel.longitude.value = selectLocationMarker.position.longitude
                _viewModel.latitude.value = selectLocationMarker.position.latitude
                _viewModel.reminderSelectedLocationStr.value = selectLocationMarker.title
                _viewModel.navigationCommand.value =
                    NavigationCommand.Back
            }
            else -> Toast.makeText(requireContext(), "You should pick a place", Toast.LENGTH_SHORT)
                .show()
        }
    }
    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!

        val latitude = 30.04253
        val longitude = 31.0934
        val latLng = LatLng(latitude, longitude)

        // Add marker on map
        mMap.addMarker(MarkerOptions().position(latLng))
        // Animating to zoom the marker
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        setPoiClick(mMap)
//        setMapLongCLick(mMap)
        setMapStyle(mMap)
        setMapLongCLick(mMap)
        enableLocation()
    }
    private fun setMapStyle(googleMap: GoogleMap?) {
        try {
            val success = googleMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
            )
            if (!success!!) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }
    private fun setPoiClick(googleMap: GoogleMap?) {
        googleMap?.setOnPoiClickListener { poi ->
            googleMap.clear()
            selectLocationMarker =
                googleMap.addMarker(MarkerOptions().position(poi.latLng).title(poi.name))
            selectLocationMarker.showInfoWindow()

            showSaveLocationBtn()
        }
    }
    private fun setMapLongCLick(googleMap: GoogleMap?) {
        googleMap?.setOnMapLongClickListener {
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                it.latitude,
                it.longitude
            )
            googleMap.clear()
            selectLocationMarker = googleMap.addMarker(
                MarkerOptions().position(it).snippet(snippet)
                    .title("Dropped Pin")
            )
            selectLocationMarker.showInfoWindow()
            showSaveLocationBtn()
        }
    }
    private fun showSaveLocationBtn() = when {
        binding.saveLocationBtn.visibility != View.INVISIBLE -> binding.saveLocationBtn.visibility =
            View.VISIBLE

        else -> binding.saveLocationBtn.visibility =
            View.VISIBLE
    }


    @SuppressLint("MissingPermission")
    private fun enableLocation() {
        try {
            if (isPermissionGranted()
            ) {
                mMap.isMyLocationEnabled = true
                Toast.makeText(
                    requireContext(),
                    "You has been accepted the permission",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                requestPermissions(
                    arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
                Toast.makeText(
                    requireContext(),
                    "You must enable your Location",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "enableLocation: ${e.message}")
        }
    }

    private fun isPermissionGranted(): Boolean {
        return context?.let {
            ContextCompat.checkSelfPermission(
                it, Manifest.permission.ACCESS_FINE_LOCATION
            )
        } == PackageManager.PERMISSION_GRANTED
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            enableLocation()
        } else {
            Snackbar.make(
                binding.mapFragment,
                R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
            ).setAction(android.R.string.ok) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
            }.show()
        }
    }


    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

}




