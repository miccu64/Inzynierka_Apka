package com.example.inzynierka_apka.services

import Location
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import java.util.*

class HubService : Service() {

    var token: String? = null

    // Binder given to clients
    private val binder = HubBinder()
    private lateinit var timer: Timer
    private val server: String = "http://192.168.2.2:45455"
    private lateinit var hubConnection: HubConnection
    private lateinit var location: Location

    inner class HubBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): HubService = this@HubService
    }

    override fun onBind(intent: Intent): IBinder {
        hubConnection = HubConnectionBuilder.create("$server/gamehub").build()
        //functions invoked from server
        hubConnection.on(
            "ErrorMessage",
            { message: String -> //Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                //textView!!.text = "response :  " + message
                Log.d("TAG", message)},
            String::class.java
        )
        hubConnection.on(
            "SuccessMessage",
            { message: String -> //Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                //textView!!.text = "response :  " + message
                Log.d("TAG", message)},
            // },
            String::class.java
        )
        hubConnection.on(
            "SaveToken",
            { message: String -> //Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                token = message
                Log.d("TAG", message)
            },
            String::class.java
        )
        hubConnection.on(
            "GetLocationFromServer",
            { message: String -> //Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                Log.d("TAG", message)
            },
            String::class.java
        )

        location = Location(this)
        connect()
        return binder
    }

    fun connect() {
        val timerTask = object : TimerTask() {
            override fun run() {
                if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
                    timer.cancel()
                    timer.purge()
                } else {
                    if (location.perms.checkInternetConnection())
                        hubConnection.start()
                    //TU MOZE JAKIES PROMPT O POLACZENIE SIE
                }
            }
        }
        //turn on timer
        timer = Timer()
        timer.schedule(timerTask, 0L, 1000L)
    }

    private fun sendLocation() {
        val timerTask = object : TimerTask() {
            override fun run() {
                if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
                    hubConnection.invoke("UpdateLocation", Location.latitude, Location.longitude, token)
                    Log.d("TAG", "timer")
                } else {
                    timer.cancel()
                    timer.purge()
                    connect()
                }
            }
        }
        //turn on timer
        timer = Timer()
        timer.schedule(timerTask, 0L, 1000L)
    }

    fun register(email: String, name: String, password: String) {
        hubConnection.invoke("RegisterNewUser", email, name, password)
    }

    fun login(email: String, password: String) {
        hubConnection.invoke("Login", email, password)
    }

    fun createRoom(roomName: String, password: String) {
        hubConnection.invoke("CreateRoom", roomName, password, token)
    }

    fun joinRoom(roomName: String, password: String) {
        hubConnection.invoke("JoinRoom", roomName, password, token)
        sendLocation()
    }

}