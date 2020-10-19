package com.example.inzynierka_apka

import Localization
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest

import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import org.json.JSONObject
import java.net.URL

data class Params (val Id: String, val Latitude: Double, val Longitude: Double)

class MainActivity : AppCompatActivity() {
    private var textView: TextView? = null
    //HTTPS nie zadziala na localhoscie - moze na zewnatrz pojdzie? pasobaloby xD
    //w AndroidManifest.xml jest dodana linia i moze nie dzialac cos przez nia
    private val server: String = "http://192.168.0.10:45455"
    private lateinit var hubConnection: HubConnection
    private var token: String? = null
    private val uriScheme = "http"
    private val uriAuthority = "192.168.0.10:45455"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById<TextView>(R.id.textView)
        hubConnection = HubConnectionBuilder.create("$server/gamehub").build()
        //ignores every certificate of SSL!!!!!!!!!!!!!!
        NukeSSLCerts.nuke()


        hubConnection.start()

    }

    private fun <T> addToQueue(request: Request<T>) {
        QueueSingleton.getInstance(this).addToRequestQueue(request)
    }

    private fun login(email: String, password: String) {
        /*val builder: Uri.Builder = Uri.Builder()
        builder.scheme(uriScheme)
            .authority(uriAuthority).
            .appendPath("user")
            .appendPath("login")
            .appendQueryParameter("email", email)
            .appendQueryParameter("password", password)

        val url = builder.build().toString()
        val resp = URL(url).readText()
        print(resp)*/
        val url = "$server/user/login?email=${email}&password=${password}"
        val request = StringRequest(Request.Method.GET, url,
            Response.Listener { response ->
                token = response
                textView!!.text = "response :  " + token
                //textView.text = "Response: %s".format(response.toString())
            },
            Response.ErrorListener { error ->
                textView!!.text = error.toString()
            }
        )
        addToQueue(request)
    }

    /*private fun postData(lat: Double, lon: Double) {
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
    }*/

    fun showLocalization(view: View) {
        login("bb", "bb")
        val local = Localization(this, this)
        val latitude: Double = Localization.latitude // latitude
        val longitude: Double = Localization.longitude // latitude
        Log.d("TAG", latitude.toString())
        val resp = hubConnection.connectionState
        //hubConnection.send("CreateRoom", "input")
        Log.d("TAG", resp.toString())

        val par = Params("Dzialaj", latitude, longitude)
        if (hubConnection.connectionState == HubConnectionState.CONNECTED)
            hubConnection.invoke("UpdateLocation", par, "aaa", 0)
    }
/*
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
    }*/

}