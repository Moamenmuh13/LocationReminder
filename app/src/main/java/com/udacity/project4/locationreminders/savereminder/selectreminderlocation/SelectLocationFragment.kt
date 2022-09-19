package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


//import com.udacity.project4.utils.setDisplayHomeAsUpEnabled

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback, View.OnClickListener {

    private val TAG = "SelectLocationFragment"
    private var mMap: GoogleMap? = null
    private lateinit var pPoi: PointOfInterest


    private val REQUEST_LOCATION_PERMISSION = 1

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
//        setDisplayHomeAsUpEnabled(true)

//        TODO: add the map setup implementation
        val supportMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        supportMapFragment?.getMapAsync(this)
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected
//        TODO: call this function after the user confirms on the selected location

        binding.saveLocationBtn.setOnClickListener(this)
        enableLocation()
        return binding.root
    }

    override fun onClick(p0: View?) {
        when (p0) {
            binding.saveLocationBtn -> {
                onLocationSelected()
            }
        }
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        if (this::pPoi.isInitialized) {
            _viewModel.longitude.value = pPoi.latLng.longitude
            _viewModel.latitude.value = pPoi.latLng.latitude
            _viewModel.reminderSelectedLocationStr.value = pPoi.name
            _viewModel.selectedPOI.value = pPoi
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SelectLocationFragmentDirections.actionSelectLocationFragmentToSaveReminderFragment())
        } else
            Toast.makeText(requireContext(), "You should pick a place", Toast.LENGTH_SHORT).show()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            mMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            mMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            mMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!

        val latitude = 30.234563
        val longitude = 31.234563
        val latLng = LatLng(latitude, longitude)

        mMap?.clear();
        // Animating to zoom the marker
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f));
        // Add marker on map
        mMap?.addMarker(MarkerOptions().position(latLng))

        setMapLongCLick(mMap)
        setPoiClick(mMap)
//        setMapStyle(mMap)
        enableLocation()
    }

//    private fun setMapStyle(googleMap: GoogleMap?) {
//        try {
//            val success = googleMap?.setMapStyle(
//                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
//            )
//            if (!success!!) {
//                Log.e(TAG, "Style parsing failed.")
//            }
//        } catch (e: Resources.NotFoundException) {
//            Log.e(TAG, "Can't find style. Error: ", e)
//        }
//    }


    private fun setPoiClick(googleMap: GoogleMap?) {
        googleMap?.setOnPoiClickListener { poi ->
            googleMap.clear()
            pPoi = poi
            val poiOptions =
                googleMap.addMarker(MarkerOptions().position(poi.latLng).title(poi.name))
            poiOptions.showInfoWindow()

            showSaveLocationBtn()
        }
    }

    private fun setMapLongCLick(googleMap: GoogleMap?) {
        googleMap?.setOnMapLongClickListener { latLng ->
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            googleMap.clear()
            googleMap.addMarker(
                MarkerOptions().position(latLng).title(getString(R.string.dropped_pin))
                    .snippet(snippet)
            )
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
        if (isPermissionGranted()) {
            mMap?.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "DONEEEEEEE ", Toast.LENGTH_SHORT).show()
                enableLocation()
            } else {
                Toast.makeText(
                    requireContext(),
                    "You must enable your Location",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}




