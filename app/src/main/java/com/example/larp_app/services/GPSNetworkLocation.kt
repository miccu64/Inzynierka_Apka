package com.example.larp_app.services

import android.content.Context
import android.location.Location

class GPSNetworkLocation(mCont: Context) {
    private val gpsLocation = LocationService(mCont, true)
    private val networkLocation = LocationService(mCont, false)

    fun getLocation(): Location? {
        if (!checkStatus()) {
            start()
        }
        if (gpsLocation.locations != null)
            return gpsLocation.locations
        return networkLocation.locations
    }

    private fun checkStatus(): Boolean {
        return gpsLocation.checkStatus() || networkLocation.checkStatus()
    }

    private fun start() {
        gpsLocation.start()
        networkLocation.start()
    }

    fun stop() {
        gpsLocation.stop()
        networkLocation.stop()
    }
}