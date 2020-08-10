package com.example.inzynierka_apka

import Localization
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.rest.QueueSingleton
import com.example.rest.ServerOperations
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    private var textView: TextView? = null
    private val server: String = "https://192.168.2.4:45455"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById<TextView>(R.id.textView)

        //ignores every certificate of SSL!!!!!!!!!!!!!!
        NukeSSLCerts.nuke()

        myGet()
    }

    private fun <T> addToQueue(request: Request<T>) {
        QueueSingleton.getInstance(this).addToRequestQueue(request)
    }

    private fun postData(lat: Double, lon: Double) {
        val url = "$server/playerinfo/send"
        val jsonObject = JSONObject()
        jsonObject.put("Id", "ddhdddddfd")
        jsonObject.put("Longitude", lon)
        jsonObject.put("Latitude", lat)


        val request = JsonObjectRequest(Request.Method.POST, url, jsonObject,
            Response.Listener { response ->
                Log.d("TAG", response.toString())
            },
            Response.ErrorListener { error ->
                Log.d("TAG", error.toString())
            }
        )
        addToQueue(request)
    }

    fun showLocalization(view: View) {
        val local = Localization(this, this)
        val latitude: Double = Localization.latitude // latitude

        val longitude: Double = Localization.longitude // latitude
        print(latitude)
        print("aaaaa")
        Log.d("TAG", latitude.toString())
        postData(latitude, longitude)
    }

    private fun myGet() {


        val url: String = "$server/playerInfo"

        val jsonObjectRequest = JsonArrayRequest(Request.Method.GET, url, null,
            Response.Listener { response ->
                textView!!.text = "response :  "
                //textView.text = "Response: %s".format(response.toString())
            },
            Response.ErrorListener { error ->
                textView!!.text = error.toString()
            }
        )

        addToQueue(jsonObjectRequest)
    }

}