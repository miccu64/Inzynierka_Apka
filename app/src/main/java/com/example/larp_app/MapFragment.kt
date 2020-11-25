package com.example.larp_app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView

private lateinit var map: MapView

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
}