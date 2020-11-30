package com.example.larp_app.ui.game

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.larp_app.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsFragment : Fragment() {

    private lateinit var map: SupportMapFragment

    fun addMarker(lat: Double, lon: Double, title: String) {
        map.getMapAsync { googleMap ->
            val coords = LatLng(lat, lon)
            googleMap.addMarker(MarkerOptions().position(coords).title(title))
        }
    }

    fun centerMap(lat: Double, lon: Double, zoom: Float) {
        map.getMapAsync { googleMap ->
            val coords = LatLng(lat, lon)
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(coords))
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(zoom))
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
        centerMap(52.0, 19.0, 12.0f)
    }
}