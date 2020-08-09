package com.example.inzynierka_apka

import Localization
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.rest.QueueSingleton
import com.example.rest.ServerOperations
import org.json.JSONArray
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    private var textView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById<TextView>(R.id.textView)

        //ignores every certificate of SSL!!!!!!!!!!!!!!
        NukeSSLCerts.nuke()

        //myGet()
        //getUsers2()
        val local = Localization(this)
        val latitude: Double = Localization.latitude // latitude

        val longitude: Double = Localization.latitude // latitude

        print(latitude)
        print("aaaaa")
    }

    fun myGet() {


        val url: String = "https://192.168.2.9:45455/playerInfo"

        val jsonObjectRequest = JsonArrayRequest(Request.Method.GET, url, null,
                Response.Listener { response ->
                    textView!!.text = "response :  "
                    //textView.text = "Response: %s".format(response.toString())
                },
                Response.ErrorListener { error ->
                    textView!!.text = error.toString()

                }
        )


        // Get a RequestQueue
        val queue = QueueSingleton.getInstance(this.applicationContext).requestQueue
        // Add a request (in this example, called stringRequest) to your RequestQueue.
        QueueSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)

        /*
        // Request a string response from the provided URL.
        val stringReq = StringRequest(Request.Method.GET, url,
            Response.Listener<String> { response ->

                var strResp = response.toString()
                val jsonObj: JSONObject = JSONObject(strResp)
                val jsonArray: JSONArray = jsonObj.getJSONArray("items")
                var str_user: String = ""

                textView!!.text = "response :  "
            },
            Response.ErrorListener { textView!!.text = "That didn't work!" })
        queue.add(stringReq)*/
    }


    fun getUsers2()
    {
        var server = ServerOperations()
        var queue = Volley.newRequestQueue(this)
        var (my,queue2) = server.myGet(queue)
        textView!!.text = my
    }

    // function for network call
    fun getUsers() {
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)
        val url: String = "https://api.github.com/search/users?q=eyehunt"

        // Request a string response from the provided URL.
        val stringReq = StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response ->

                    var strResp = response.toString()
                    val jsonObj: JSONObject = JSONObject(strResp)
                    val jsonArray: JSONArray = jsonObj.getJSONArray("items")
                    var str_user: String = ""
                    for (i in 0 until jsonArray.length()) {
                        var jsonInner: JSONObject = jsonArray.getJSONObject(i)
                        str_user = str_user + "\n" + jsonInner.get("login")
                    }
                    textView!!.text = "response : $str_user "
                },
                Response.ErrorListener { textView!!.text = "That didn't work!" })
        queue.add(stringReq)
    }
}