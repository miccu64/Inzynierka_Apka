package com.example.larp_app.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import com.example.larp_app.others.MyPermissions


class LocationService(mCont: Context) : Service(), LocationListener {
    private var isGPSEnabled = false
    private var isNetworkEnabled = false
    private var locations: Location? = null
    private var locationManager: LocationManager? = null
    private val mContext: Context = mCont
    val perms: MyPermissions = MyPermissions(mCont)

    //SuppressLint used, bcs in another place there are checked permissions
    @SuppressLint("MissingPermission")
    private fun getLocation() {
        try {
            locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled

            } else {
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    if (!perms.checkPermissions())
                        return
                    locationManager!!.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        minTime,
                        minDistance,
                        this
                    )
                    locations =
                        locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }

                if (isGPSEnabled && locations == null) {
                    locationManager!!.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        minTime,
                        minDistance,
                        this
                    )
                    locations = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                }
                latitude = locations!!.latitude
                longitude = locations!!.longitude
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onLocationChanged(location: Location) {
        latitude = location.latitude
        longitude = location.longitude
        locations = location
    }

    override fun onProviderDisabled(provider: String) {
        getLocation()
    }

    override fun onProviderEnabled(provider: String) {
        getLocation()
    }

    override fun onStatusChanged(
        provider: String,
        status: Int,
        extras: Bundle
    ) {
        getLocation()
    }

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    //singleton
    companion object {
        var latitude = 0.0
        var longitude = 0.0

        //min distance to change updates in meters
        private const val minDistance: Float = 0.1F // 0.1 meters (distance accuracy)

        //min time between updates in milliseconds
        private const val minTime = 1000 * 2.toLong() // every 2 seconds update
    }

    init {
        getLocation()
    }
}