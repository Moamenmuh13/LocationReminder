package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import org.koin.android.ext.android.inject


private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val TAG = "SaveReminderFragment"
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1

@SuppressLint("UnspecifiedImmutableFlag")
class SaveReminderFragment : BaseFragment() {
    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "HuntMainActivity.treasureHunt.action.ACTION_GEOFENCE_EVENT"
    }

    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    private lateinit var geofenceClient: GeofencingClient
    private lateinit var reminderDataItem: ReminderDataItem

    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

//        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        geofenceClient = LocationServices.getGeofencingClient(requireContext())

        binding.selectLocation.setOnClickListener { selectLocation() }
        binding.saveReminder.setOnClickListener { setReminderDataToLocalDB() }
    }


    private fun selectLocation() {
        //            Navigate to another fragment to get the user location
        _viewModel.navigationCommand.value =
            NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
    }

    private fun setReminderDataToLocalDB() {
        val title = _viewModel.reminderTitle.value
        val description = _viewModel.reminderDescription.value
        val location = _viewModel.reminderSelectedLocationStr.value
        val latitude = _viewModel.latitude.value
        val longitude = _viewModel.longitude.value

        reminderDataItem = ReminderDataItem(title, description, location, latitude, longitude)
        if (_viewModel.validateAndSaveReminder(reminderDataItem)) {
            checkPermissionAndStartGeofencing()
        }

    }

    private fun checkPermissionAndStartGeofencing() {
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            checkDeviceLocationSettingAndStartGeofence()
        } else {
            requestForegroundAndBackgroundLocationPermission()
        }
    }

    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved =
            (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ))
        val backgroundPermissionApproved = if (runningQOrLater) {
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            true
        }
        return foregroundLocationApproved && backgroundPermissionApproved
    }

    @TargetApi(android.os.Build.VERSION_CODES.Q)
    private fun requestForegroundAndBackgroundLocationPermission() {
        if (foregroundAndBackgroundLocationPermissionApproved())
            return

        // Else request the permission
        // this provides the result[LOCATION_PERMISSION_INDEX]
        var permissionArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        val resultCode = when {
            runningQOrLater -> {
                permissionArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }

        Log.d(TAG, resultCode.toString())
        ActivityCompat.requestPermissions(requireActivity(), permissionArray, resultCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isEmpty() ||
            grantResults[0] == PackageManager.PERMISSION_DENIED
            || (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
                    && grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX]
                    == PackageManager.PERMISSION_DENIED)
        ) {
            Toast.makeText(
                requireContext(),
                getString(R.string.location_required_error),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            checkDeviceLocationSettingAndStartGeofence()
        }
    }

    private fun checkDeviceLocationSettingAndStartGeofence(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingResponse = settingClient.checkLocationSettings(builder.build())
        locationSettingResponse.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog
                try {
                    exception.startResolutionForResult(
                        requireActivity(),
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendException: IntentSender.SendIntentException) {
                    Log.d(
                        TAG,
                        "errorSendingLocation: ${sendException.message} "
                    )
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.location_required_error),
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
        locationSettingResponse.addOnCompleteListener {
            if (it.isSuccessful) {
                addGeofenceForReminder()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeofenceForReminder() {
        if (this::reminderDataItem.isInitialized) {
            val currentGeofenceData = reminderDataItem

            val geofence = Geofence.Builder()
                .setRequestId(currentGeofenceData.id)
                .setCircularRegion(
                    currentGeofenceData.latitude!!,
                    currentGeofenceData.longitude!!,
                    2000f
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()

            val geofenceRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

            val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
            intent.action = ACTION_GEOFENCE_EVENT

            val geofencePendingIntent = startGeofencingBroadcastReceiver(intent)

            geofenceClient.removeGeofences(geofencePendingIntent)?.run {
                addOnCompleteListener {
                    geofenceClient.addGeofences(geofenceRequest, geofencePendingIntent).run {
                        addOnSuccessListener {
                            Log.d(TAG, "addGeofenceForReminder: ${geofence.requestId}")
                            Toast.makeText(requireContext(), geofence.requestId, Toast.LENGTH_SHORT)
                                .show()
                        }
                        addOnFailureListener {
                            if (it.message != null) {
                                Log.d(TAG, "Failed To add geofence: ${it.message}")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startGeofencingBroadcastReceiver(intent: Intent): PendingIntent? {
        return PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }


    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

}

