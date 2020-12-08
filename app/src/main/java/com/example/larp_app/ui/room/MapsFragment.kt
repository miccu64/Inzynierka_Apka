package com.example.larp_app.ui.room

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.larp_app.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONArray
import org.json.JSONObject

class MapsFragment : Fragment() {

    private lateinit var map: SupportMapFragment
    private val markers = mutableMapOf<String, Marker>()

    fun showOnMap(arrString: String) {
        val arr = JSONArray(arrString)
        for (i in 0 until arr.length()) {
            val el = arr[i] as JSONObject
            val name = el.getString("PlayerName") as String
            val coords = LatLng(el.getDouble("Latitude"), el.getDouble("Longitude"))
            Handler(Looper.getMainLooper()).post {
                if (markers.containsKey(name)) {
                    val marker = markers[name] as Marker
                    marker.position = coords
                    markers.replace(name, marker)
                } else map.getMapAsync { googleMap ->
                    markers[name] = googleMap.addMarker(MarkerOptions().position(coords).title(name))
                }
            }
        }
    }

    private fun centerMap(lat: Double, lon: Double, zoom: Float) {
        Handler(Looper.getMainLooper()).post {
            map.getMapAsync { googleMap ->
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), zoom))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        map = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

    }

    override fun onStart() {
        super.onStart()

        centerMap(50.08260, 19.95853, 7.0f)
    }
}