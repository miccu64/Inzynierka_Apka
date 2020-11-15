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


class Location(mCont: Context) : Service(), LocationListener {
    var isGPSEnabled = false
    var isNetworkEnabled = false
    var locations : Location? = null
    private var locationManager: LocationManager? = null
    private val mContext: Context = mCont
    val perms: MyPermissions = MyPermissions(mCont)


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
                    if (!perms.checkPermissions()) return null
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