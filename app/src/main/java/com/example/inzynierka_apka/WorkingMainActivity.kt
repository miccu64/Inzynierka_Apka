package com.example.inzynierka_apka


import android.app.Activity
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView


class WorkingMainActivity : Activity(), LocationListener {
    protected var locationManager: LocationManager? = null
    protected var locationListener: LocationListener? = null
    protected var context: Context? = null

    var lat: Double? = null
    var provider: String? = null
    protected var latitude: Double? = null
    protected var longitude: Double? = null
    protected var gps_enabled = false
    protected var network_enabled = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
    }

    override fun onLocationChanged(location: Location) {
        latitude = location.getLatitude()
        longitude = location.getLongitude()
    }

    override fun onProviderDisabled(provider: String) {
        Log.d("Latitude", "disable")
    }

    override fun onProviderEnabled(provider: String) {
        Log.d("Latitude", "enable")
    }

    override fun onStatusChanged(
        provider: String,
        status: Int,
        extras: Bundle
    ) {
        Log.d("Latitude", "status")
    }
}