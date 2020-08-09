package com.example.rest

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject


class ServerOperations {

    public fun myGet(queue : RequestQueue) : Pair<String, RequestQueue>
    {
        //val url: String = "https://192.168.2.2:45455/api/NewDatas/"
        // Instantiate the RequestQueue.

        val url: String = "https://api.github.com/search/users?q=eyehunt"

        var result = "EEEEE"
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
                result = "response : $str_user "
            },
            Response.ErrorListener { result = "That didn't work!" })
        queue.add(stringReq)
        return Pair(result,queue)
    }
}