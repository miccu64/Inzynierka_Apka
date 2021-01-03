package com.example.larp_app.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import com.example.larp_app.others.MyPermissions


class LocationService(mCont: Context, fromGPS: Boolean) : Service(), LocationListener {
    private val mContext: Context = mCont
    private var provider: String = if (fromGPS) {
        LocationManager.GPS_PROVIDER
    } else LocationManager.NETWORK_PROVIDER

    //min distance to change updates in meters
    private val minDistance: Float = 0.1F // 0.1 meters (distance accuracy)

    //min time between updates in milliseconds
    private val minTime = 1000 * 1.toLong() // every 1 second update

    private val perms: MyPermissions = MyPermissions(mCont)

    var locations: Location? = null
    private lateinit var locationManager: LocationManager
    private var isRunning = false

    fun checkStatus(): Boolean {
        return isRunning && locations != null
    }

    fun start() {
        getLocation()
    }

    fun stop() {
        if (isRunning) {
            locationManager.removeUpdates(this)
            isRunning = false
        }
    }

    private fun makeToast(text: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(mContext, text, Toast.LENGTH_LONG).show()
        }
    }

    //SuppressLint used, bcs in another place there are checked permissions
    @SuppressLint("MissingPermission")
    private fun getLocation() {
        try {
            locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            if (!perms.checkPermissions()) {
                makeToast("Musisz udzielić uprawnienia dla aplikacji")
                return
            }
            if (!locationManager.isProviderEnabled(provider)) {
                isRunning = false
                makeToast("Musisz uruchomić lokalizację")
                return
            }
            //update location on change automatically
            Handler(Looper.getMainLooper()).post {
                locationManager.requestLocationUpdates(provider, minTime, minDistance, this)
            }
            locations = locationManager.getLastKnownLocation(provider)
            isRunning = true
        } catch (e: Exception) {
            isRunning = false
        }
    }

    override fun onLocationChanged(location: Location) {
        locations = location
    }

    override fun onProviderDisabled(provider: String) {
    }

    override fun onProviderEnabled(provider: String) {
        getLocation()
    }

    override fun onStatusChanged(
        provider: String,
        status: Int,
        extras: Bundle
    ) {
    }

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }
}