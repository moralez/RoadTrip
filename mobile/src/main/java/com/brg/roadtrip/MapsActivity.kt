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
import com.google.android.gms.location.LocationServices
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.LocationRequest

class MapsActivity : FragmentActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // MARK: Constants

    private var MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 4000

    // MARK: Properties

    private var mMap: GoogleMap? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLastLocation: Location? = null

    // MARK: Lifecycle Methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build()
        }
    }

    override fun onStart() {
        mGoogleApiClient?.connect()
        super.onStart()
    }

    override fun onStop() {
        mGoogleApiClient?.disconnect()
        super.onStop()
    }


    // MARK: OnMapReadyCallback Interface Methods

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

    // MARK: GoogleApiClient.ConnectionCallbacks Interface Methods

    override fun onConnected(p0: Bundle?) {
        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (PackageManager.PERMISSION_GRANTED == permissionCheck) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
            if (mLastLocation != null) {
                Log.i("JMO", "Got a location! " + mLastLocation)
                handleLastLocation(mLastLocation!!)
//                mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
//                mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
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

    // MARK: GoogleApiClient.OnConnectionFailedListener Interface Methods

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // MARK: Permission Request Handling

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

    // MARK: Location Handling

    fun createLocationRequest() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.interval = 10000
        mLocationRequest.fastestInterval = 5000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    fun handleLastLocation(location: Location) {
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 15.0f))
    }
}
