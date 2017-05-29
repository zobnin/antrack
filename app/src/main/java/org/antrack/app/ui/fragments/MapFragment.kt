package org.antrack.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.R
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.antrack.app.libs.L
import org.antrack.app.ui.State
import org.antrack.app.ui.U
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException

class MapFragment : BaseFragment(), OnMapReadyCallback {
    internal val TAG = "MapFragment"

    override val module = "locate"

    lateinit private var mapView: MapView
    lateinit private var currentLocation: Location

    private inner class Location {
        internal var date = "0"
        internal var time = "0"
        internal var lat = "0"
        internal var lng = "0"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Otherwise GetActivity() return null after orientation change
        retainInstance = true

        checkModule() || return null

        val view = inflater.inflate(R.layout.fragment_mapview, container, false)

        currentLocation = Location()

        mapView = view.findViewById(R.id.fragment_mapview_map) as MapView
        mapView.onCreate(savedInstanceState)

        onFileUpdate()

        U.runCommandAsync(command!!)
        if (!State.device!!.isMain) {
            U.getFileAsync(watchFile!!)
        }

        return view
    }

    override fun onFileUpdate() {
        Thread(Runnable {
            if (!readFile()) {
                showNoDataOrLoading()
                return@Runnable
            }

            if (activity == null) return@Runnable
            activity.runOnUiThread {
                mapView.visibility = View.VISIBLE
                mapView.getMapAsync(this@MapFragment)
                hideAllMessages()
            }
        }).start()
    }

    override fun onMapReady(map: GoogleMap) {
        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this.activity)

        map.uiSettings.isMyLocationButtonEnabled = false
        map.isMyLocationEnabled = true

        // Clear map from previous markers
        map.clear()

        // Add marker
        map.addMarker(MarkerOptions()
                .position(LatLng(java.lang.Double.parseDouble(currentLocation.lat), java.lang.Double.parseDouble(currentLocation!!.lng)))
                .title(currentLocation.date + " " + currentLocation.time))

        // Updates the location and zoom of the MapView
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(java.lang.Double.parseDouble(currentLocation!!.lat), java.lang.Double.parseDouble(currentLocation!!.lng)), 15f)
        map.animateCamera(cameraUpdate)

    }

    private fun readFile(): Boolean {
        val path = U.getLocalPath(watchFile!!)

        if (!File(path).exists()) {
            return false
        }

        try {
            val reader = BufferedReader(FileReader(U.getLocalPath(watchFile!!)))
            reader.readLines().forEach { line ->
                val loc = line.split(" ".toRegex())
                if (loc.size < 4)
                    return@forEach
                currentLocation.date = loc[0]
                currentLocation.time = loc[1]
                currentLocation.lat = loc[2]
                currentLocation.lng = loc[3]
            }
        } catch (e: IOException) {
            L.e(TAG, "Can't read apps file: " + e)
            return false
        }

        return true
    }

    override fun onResume() {
        if (mapView != null) {
            mapView!!.onResume()
        }
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (mapView != null) {
            mapView!!.onPause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mapView != null) {
            mapView!!.onDestroy()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        if (mapView != null) {
            mapView!!.onLowMemory()
        }
    }
}
