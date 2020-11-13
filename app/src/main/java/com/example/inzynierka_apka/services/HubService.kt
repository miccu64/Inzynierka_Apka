package com.example.inzynierka_apka.services

import Location
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.inzynierka_apka.MainActivity
import com.example.inzynierka_apka.R
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import java.util.*


class HubService : Service() {

    var token: String? = null

    // Binder given to clients
    private val binder = HubBinder()
    private lateinit var timer: Timer

    //private val server: String = "http://192.168.0.10:45455"
    private val server: String = "http://192.168.2.10:45455"
    private lateinit var hubConnection: HubConnection
    private lateinit var location: Location
    private var count = 0

    inner class HubBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): HubService = this@HubService
    }

    private fun showToast(text: String) {
        val myContext: Context = this
        Handler(Looper.getMainLooper()).post {
            val toast1 = Toast.makeText(myContext, text, Toast.LENGTH_LONG)
            toast1.show()
        }
    }

    override fun onBind(intent: Intent): IBinder {
        hubConnection = HubConnectionBuilder.create("$server/gamehub").build()
        //functions invoked from server
        hubConnection.on(
            "ErrorMessage",
            { message: String ->
                showToast(message)
            }, String::class.java
        )
        hubConnection.on(
            "SuccessMessage",
            { message: String ->
                showToast(message)
            },
            String::class.java
        )
        hubConnection.on(
            "RegisterSuccess",
            { message: String ->
                showToast(message)

            }, String::class.java
        )
        hubConnection.on(
            "SaveToken",
            { message: String ->
                token = message
                Log.d("TAG", message)
            },
            String::class.java
        )
        hubConnection.on(
            "GetLocationFromServer",
            { message: String ->
                Log.d("TAG", message)
            },
            String::class.java
        )

        location = Location(this)
        connect()
        return binder
    }

    private fun checkHubConnection(): Boolean {
        return if (hubConnection.connectionState == HubConnectionState.CONNECTED)
            true
        else {
            connect()
            false
        }
    }

    fun connect() {
        count = 0
        val timerTask = object : TimerTask() {
            override fun run() {
                if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
                    timer.cancel()
                    timer.purge()
                } else {
                    if (location.perms.checkInternetConnection()) {
                        if (count == 5) {
                            showToast("Brak połączenia z serwerem.")
                        }
                        count++
                        hubConnection.start()
                    } else if (count == 0)
                        showToast("Uruchom transmisję danych.")
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
                    hubConnection.invoke(
                        "UpdateLocation",
                        Location.latitude,
                        Location.longitude,
                        token
                    )
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
        if (checkHubConnection())
            hubConnection.invoke("RegisterNewUser", email, name, password)
    }

    fun login(email: String, password: String) {
        if (checkHubConnection())
            hubConnection.invoke("Login", email, password)
    }

    fun createRoom(roomName: String, password: String) {
        if (checkHubConnection())
            hubConnection.invoke("CreateRoom", roomName, password, token)
    }

    fun joinRoom(roomName: String, password: String) {
        if (checkHubConnection()) {
            hubConnection.invoke("JoinRoom", roomName, password, token)
            sendLocation()
        }
    }

}