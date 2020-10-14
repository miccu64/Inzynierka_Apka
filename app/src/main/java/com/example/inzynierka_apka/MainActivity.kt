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
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import org.json.JSONObject
import java.lang.Thread.sleep


class MainActivity : AppCompatActivity() {
    private var textView: TextView? = null
    //HTTPS nie zadziala na localhoscie - moze na zewnatrz pojdzie? pasobaloby xD
    //w AndroidManifest.xml jest dodana linia i moze nie dzialac cos przez nia
    private val server: String = "http://192.168.0.10:45455"
    private lateinit var hubConnection: HubConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById<TextView>(R.id.textView)
        hubConnection = HubConnectionBuilder.create("$server/gamehub").build()
        //ignores every certificate of SSL!!!!!!!!!!!!!!
        NukeSSLCerts.nuke()
        myGet()

        hubConnection.start()

    }

    private fun <T> addToQueue(request: Request<T>) {
        QueueSingleton.getInstance(this).addToRequestQueue(request)
    }

    private fun postData(lat: Double, lon: Double) {
        val url = "$server/coords/register"
        val jsonObject = JSONObject()
        jsonObject.put("Id", "DAAAAXDD")
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
        //"Ciagle jest Disconnected :C"
        hubConnection.stop()
        hubConnection.start()
        val resp = hubConnection.connectionState
        sleep(5_000)
        //hubConnection.send("CreateRoom", "input")
        Log.d("TAG", resp.toString())

    }

    private fun myGet() {


        val url: String = "$server/coords/getById/aaa"

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