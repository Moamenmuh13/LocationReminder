package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
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
import kotlinx.android.synthetic.main.it_reminder.*
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback, View.OnClickListener {

    private val TAG = "SelectLocationFragment"
    private lateinit var mMap: GoogleMap
    private lateinit var pPoi: PointOfInterest


    private val REQUEST_LOCATION_PERMISSION = 1

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
//        setDisplayHomeAsUpEnabled(true)

        val supportMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        supportMapFragment?.getMapAsync(this)

        binding.saveLocationBtn.setOnClickListener(this)
//        enableLocation()
        return binding.root
    }

    override fun onClick(p0: View?) {
        when (p0) {
            binding.saveLocationBtn -> {
                onLocationSelected()
            }
        }
    }


    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!

        val latitude = 37.4253
        val longitude = -122.0934
        val latLng = LatLng(latitude, longitude)

        // Add marker on map
        mMap.addMarker(MarkerOptions().position(latLng))
        // Animating to zoom the marker
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        enableLocation()

        setPoiClick(mMap)
//        setMapLongCLick(mMap)
        setMapStyle(mMap)
    }

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

//    private fun setMapLongCLick(googleMap: GoogleMap?) {
//        googleMap?.setOnMapLongClickListener { latLng ->
//            val snippet = String.format(
//                Locale.getDefault(),
//                "Lat: %1$.5f, Long: %2$.5f",
//                latLng.latitude,
//                latLng.longitude
//            )
//            googleMap.clear()
//            googleMap.addMarker(
//                MarkerOptions().position(latLng).snippet(snippet)
//                    .title("Dropped Pin")
//            )
//        }
//    }

    private fun onLocationSelected() {
        when {
            this::pPoi.isInitialized -> {
                _viewModel.longitude.value = pPoi.latLng.longitude
                _viewModel.latitude.value = pPoi.latLng.latitude
                _viewModel.reminderSelectedLocationStr.value = pPoi.name
                _viewModel.selectedPOI.value = pPoi
                _viewModel.navigationCommand.value =
                    NavigationCommand.To(SelectLocationFragmentDirections.actionSelectLocationFragmentToSaveReminderFragment())
            }
            else -> Toast.makeText(requireContext(), "You should pick a place", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun showSaveLocationBtn() = when {
        binding.saveLocationBtn.visibility != View.INVISIBLE -> binding.saveLocationBtn.visibility =
            View.VISIBLE

        else -> binding.saveLocationBtn.visibility =
            View.VISIBLE
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


    private fun enableLocation() {
        when (PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            -> {
                mMap.isMyLocationEnabled = true
                Toast.makeText(
                    requireContext(),
                    "You has been accepted the permission",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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




