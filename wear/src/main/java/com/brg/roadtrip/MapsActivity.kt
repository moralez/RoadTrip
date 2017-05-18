package com.brg.roadtrip

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.support.wearable.view.DismissOverlayView
import android.view.View
import android.view.WindowInsets
import android.widget.FrameLayout

class MapsActivity : WearableActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    /**
     * Overlay that shows a short help text when first launched. It also provides an option to
     * exit the app.
     */
    private var mDismissOverlay: DismissOverlayView? = null

    /**
     * The map. It is initialized when the map has been fully loaded and is ready to be used.

     * @see .onMapReady
     */
    private var mMap: GoogleMap? = null

    public override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)

        setAmbientEnabled()

        // Set the layout. It only contains a MapFragment and a DismissOverlay.
        setContentView(R.layout.activity_maps)

        // Retrieve the containers for the root of the layout and the map. Margins will need to be
        // set on them to account for the system window insets.
        val topFrameLayout = findViewById(R.id.root_container) as FrameLayout
        val mapFrameLayout = findViewById(R.id.map_container) as FrameLayout

        // Set the system view insets on the containers when they become available.
        topFrameLayout.setOnApplyWindowInsetsListener { v, insets ->
            var insets = insets
            // Call through to super implementation and apply insets
            insets = topFrameLayout.onApplyWindowInsets(insets)

            val params = mapFrameLayout.layoutParams as FrameLayout.LayoutParams

            // Add Wearable insets to FrameLayout container holding map as margins
            params.setMargins(
                    insets.systemWindowInsetLeft,
                    insets.systemWindowInsetTop,
                    insets.systemWindowInsetRight,
                    insets.systemWindowInsetBottom)
            mapFrameLayout.layoutParams = params

            insets
        }

        // Obtain the DismissOverlayView and display the introductory help text.
        mDismissOverlay = findViewById(R.id.dismiss_overlay) as DismissOverlayView
        mDismissOverlay!!.setIntroText(R.string.intro_text)
        mDismissOverlay!!.showIntroIfNecessary()

        // Obtain the MapFragment and set the async listener to be notified when the map is ready.
        val mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        // Map is ready to be used.
        mMap = googleMap

        // Set the long click listener as a way to exit the map.
        mMap!!.setOnMapLongClickListener(this)

        // Add a marker in Sydney, Australia and move the camera.
        val sydney = LatLng(-34.0, 151.0)
        mMap!!.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    override fun onMapLongClick(latLng: LatLng) {
        // Display the dismiss overlay with a button to exit this activity.
        mDismissOverlay!!.show()
    }
}
