import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.ActivityCompat


class Localization(private val mContext: Context, activ: Activity) : Service(), LocationListener {
    var isGPSEnabled = false
    var isNetworkEnabled = false
    var locations : Location? = null
    //needed activity from main thread to request permissions
    private var activity: Activity = activ

    private var locationManager: LocationManager? = null

    private fun checkPermissions(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            mContext,
            Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            mContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    private fun grantPermissions(): Boolean {
        //checks permissions and ask for them if needed
        if (!checkPermissions())
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION), 1337)
        else return true
        return checkPermissions()
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(): Location? {
        try {
            locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGPSEnabled && !isNetworkEnabled) { // no network provider is enabled
            } else {
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    if (!grantPermissions()) return null
                    locationManager!!.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        minTime,
                        minDistance,
                        this
                    )
                    locations = locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
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
        return locations
    }

    override fun onLocationChanged(location: Location) {
        latitude = location.latitude
        longitude = location.longitude
        locations = location
    }
    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onStatusChanged(
        provider: String,
        status: Int,
        extras: Bundle
    ) {
    }

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    //exists only one companion object between different instances of the same class
    companion object {
        var latitude = 0.0
        var longitude = 0.0
        //min distance to change updates in meters
        private const val minDistance: Float = 0.1F // 0.1 meters (distance accuracy)
        //min time between updates in milliseconds
        private const val minTime = 1000 * 1 .toLong() // every 1 second update
    }

    init {
        getLocation()
    }
}