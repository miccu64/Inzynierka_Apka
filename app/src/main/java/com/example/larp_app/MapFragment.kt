package com.example.larp_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.OverlayItem


private lateinit var map: MapView
private val points: ArrayList<OverlayItem> = ArrayList()

class MapFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        map = view.findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        centerMap(52.0, 19.0)
        addPoint(2.0, 19.0,"a","b")

        //load/initialize the osmdroid configuration
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
    }

    override fun onResume() {
        super.onResume()
        map.onResume();
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    fun centerMap(lat: Double, lon: Double) {
        val mapController = map.controller
        mapController.setZoom(7.0)
        val startPoint = GeoPoint(lat, lon)
        mapController.setCenter(startPoint)
    }

     fun addPoint(lat: Double, lon: Double, title: String, desc: String) {
         val startPoint = GeoPoint(lat, lon)
         val startMarker = Marker(map)
         startMarker.position = startPoint
         startMarker.setAnchor(
             Marker.ANCHOR_CENTER,
             Marker.ANCHOR_BOTTOM
         )
         map.overlays.add(startMarker)
         points.add(
             OverlayItem(
                 title,
                 desc,
                 GeoPoint(lat, lon)
             )
         )

     }
}