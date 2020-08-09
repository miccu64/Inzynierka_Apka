import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder

class Localization(private val mContext: Context) : Service(),
    LocationListener {
    // flag for GPS status
    var isGPSEnabled = false
    // flag for network status
    var isNetworkEnabled = false
    var canGetLocation = false
    var locations : Location? = null
    // Declaring a Location Manager
    protected var locationManager: LocationManager? = null

    fun getLocation(): Location? {
        try {
            locationManager =
                mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            // getting GPS status
            isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            // getting network status
            isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (!isGPSEnabled && !isNetworkEnabled) { // no network provider is enabled
            } else {
                canGetLocation = true
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager!!.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(),
                        this
                    )
                    //                  Log.d("Network", "Network");
                    if (locationManager != null) {
                        locations =
                            locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        if (locations != null) {
                            latitude = locations!!.latitude
                            longitude = locations!!.longitude
                            //                          System.out.println("latitude    if (isNetworkEnabled)   => "+latitude);
//                          System.out.println("longitude   if (isNetworkEnabled)   => "+longitude);
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (locations == null) {
                        locationManager!!.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(),
                            this
                        )
                        //                      Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            locations =
                                locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            if (locations != null) {
                                latitude = locations!!.latitude
                                longitude = locations!!.longitude
                                //                              System.out.println("latitude    if (isGPSEnabled)   => "+latitude);
//                              System.out.println("longitude   if (isGPSEnabled)   => "+longitude);
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return locations
    }

    override fun onLocationChanged(location: Location) {}
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

    companion object {
        var latitude // latitude
                = 0.0
        var longitude // longitude
                = 0.0
        // The minimum distance to change Updates in meters
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10 // 10 meters
        // The minimum time between updates in milliseconds
        private const val MIN_TIME_BW_UPDATES = 1000 * 60 * 1 // 1 minute
            .toLong()
    }

    init {
        getLocation()
    }
}