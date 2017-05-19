package com.brg.roadtrip

import android.Manifest
import android.location.Location
import android.support.v4.app.FragmentActivity
import android.os.Bundle
import com.google.android.gms.common.ConnectionResult

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.common.api.GoogleApiClient
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.brg.roadtrip.model.LandmarkResponse
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DateFormat
import java.util.*


class MapsActivity : FragmentActivity(), OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<LocationSettingsResult>, LocationListener {

    //region Constants

    private var MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 4000

    //endregion

    //region Properties

    private var mMap: GoogleMap? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLastLocation: Location? = null
    private var mLastUpdateTime: String? = null
    private var mRequestingLocationUpdates: Boolean = true

    //endregion

    //region UI Elements

    private var mLastKnownLatitude: TextView? = null
    private var mLastKnownLongitude: TextView? = null
    private var mLastKnownTime: TextView? = null

    //endregion

    //region Lifecycle Methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build()
        }

        mLastKnownLatitude = findViewById(R.id.last_known_latitude) as TextView?
        mLastKnownLongitude = findViewById(R.id.last_known_longitude) as TextView?
        mLastKnownTime = findViewById(R.id.last_known_time) as TextView?
    }

    override fun onStart() {
        mGoogleApiClient?.connect()
        super.onStart()
    }

    override fun onStop() {
        mGoogleApiClient?.disconnect()
        super.onStop()
    }

    //endregion

    //region OnMapReadyCallback Interface Methods

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap!!.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    //endregion

    //region GoogleApiClient.ConnectionCallbacks Interface Methods

    override fun onConnected(p0: Bundle?) {
        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (PackageManager.PERMISSION_GRANTED == permissionCheck) {

            val builder = LocationSettingsRequest.Builder().addLocationRequest(createLocationRequest())
            val result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                    builder.build())

            if (mRequestingLocationUpdates) {
                startLocationUpdates()
            }

            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
            if (mLastLocation != null) {
                handleLastLocation(mLastLocation!!)
            }
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                val alertDialog = AlertDialog.Builder(this@MapsActivity).create()
                alertDialog.setTitle("Location Permission Request")
                alertDialog.setMessage("We have to know where you are to tell you what's around you...")
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "DISMISS",
                        { dialog, _ ->
                            dialog.dismiss()
                            requestPermissions()
                        })
                alertDialog.show()
            } else {
                requestPermissions()
            }
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    //endregion

    //region GoogleApiClient.OnConnectionFailedListener Interface Methods

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    //endregion

    //region Permission Request Handling

    fun requestPermissions() {
        ActivityCompat.requestPermissions(this@MapsActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
                    } catch (e: SecurityException) {
                        Toast.makeText(this, "ERROR: Permissions Issue", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(this, "Permission Denied :(", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    //endregion

    //region ResultCallback Interface Methods

    override fun onResult(result: LocationSettingsResult) {
        var status = result.status
        var locationSettingsStates = result.locationSettingsStates
        when (status.statusCode) {
            LocationSettingsStatusCodes.SUCCESS -> {
                Log.i("JMO", "Good")
            }
            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                Log.i("JMO", "Bad")
            }
            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                Log.i("JMO", "Unclear")
            }
        }
    }

    //endregion

    //region LocationListener Interface Methods

    override fun onLocationChanged(location: Location?) {
        mLastLocation = location
        mLastUpdateTime = DateFormat.getTimeInstance().format(Date())
        handleLastLocation(location!!)
    }

    //endregion

    //region Location Handling

    fun createLocationRequest(): LocationRequest {
        val mLocationRequest = LocationRequest()
        mLocationRequest.interval = 10000
        mLocationRequest.fastestInterval = 5000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        return mLocationRequest
    }

    fun handleLastLocation(location: Location) {
        mMap?.clear()
        val convertedLocation = LatLng(location.latitude, location.longitude)
        mMap?.addMarker(MarkerOptions().position(convertedLocation).title("Last Known Location"))
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(convertedLocation, 12.0f))

        mLastKnownLatitude?.text = location.latitude.toString()
        mLastKnownLongitude?.text = location.longitude.toString()
        mLastKnownTime?.text = location.time.toString()

        getNearbyLandmarks(location)
    }

    fun startLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, createLocationRequest(), this)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    //endregion

    //region Networking Calls

    fun getNearbyLandmarks(location: Location) {
        val retrofit = Retrofit.Builder()
                .baseUrl("http://api.geckolandmarks.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        val service = retrofit.create(RoadTripNetworkingService::class.java)
        service.getLandmarks(location.latitude, location.longitude).enqueue(object : Callback<LandmarkResponse> {
            override fun onFailure(call: Call<LandmarkResponse>?, t: Throwable?) {
                Log.i("JMO", "Failure!")
            }

            override fun onResponse(call: Call<LandmarkResponse>?, response: Response<LandmarkResponse>?) {
                val landmarkResponse = response?.body() as? LandmarkResponse
                val landmarks = landmarkResponse?.landmarks
                val iterator = landmarks?.listIterator()
                mMap?.clear()
                while (null != iterator && iterator.hasNext()) {
                    val landmark = iterator.next()
                    val convertedLocation = LatLng(landmark.lat!!, landmark.lon!!)
                    mMap?.addMarker(MarkerOptions().position(convertedLocation).title(landmark.name1!!))
                }

                val convertedLocation = LatLng(mLastLocation?.latitude!!, mLastLocation?.longitude!!)
                mMap?.addMarker(MarkerOptions().position(convertedLocation).title("Last Known Location"))
            }
        })
    }
}
