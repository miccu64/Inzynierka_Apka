package com.example.larp_app.services

import Location
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.microsoft.signalr.Action1
import com.microsoft.signalr.Action2
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import java.util.*


class HubService : Service() {
    var token: String? = null

    // Binder given to clients
    private val binder = HubBinder()
    private lateinit var timer: Timer
    private var callback: IHubCallback? = null
    private var joinedRoomName: String? = null


    private val server: String = "http://192.168.0.10:45455"
    //private val server: String = "http://192.168.2.10:45455"
    private lateinit var hubConnection: HubConnection
    private lateinit var location: Location
    private var count = 0

    inner class HubBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): HubService = this@HubService
    }

    fun setCallbacks(call: IHubCallback?) {
        callback = call
    }

    override fun onCreate() {
        super.onCreate()

        hubConnection = HubConnectionBuilder.create("$server/gamehub").build()
        //functions invoked from server
        hubConnection.on(
            "ErrorMessage",
            { message: String ->
                callback?.showToast(message)
            }, String::class.java
        )
        hubConnection.on(
            "SuccessMessage",
            { message: String ->
                callback?.showToast(message)
            },
            String::class.java
        )
        hubConnection.on(
            "RegisterSuccess",
            { message: String ->
                callback?.showDialog(message, "Możesz się zalogować")
                callback?.goToLogin2()
            }, String::class.java
        )
        hubConnection.on(
            "GetLocationFromServer",
            { message: String ->
                Log.d("TAG", message)
            },
            String::class.java
        )
        hubConnection.on(
            "LoginSuccess",
            { tok: String, roomList: String ->
                token = tok
                callback?.loginSuccess(roomList)
            },
            String::class.java, String::class.java
        )
        hubConnection.on(
            "LoginRegisterError",
            { message: String ->
                callback?.showToast(message)
                callback?.hideDialog()
            },
            String::class.java
        )
        hubConnection.on(
            "GoToLogin",
            { message: String ->
                callback?.showToast(message)
                callback?.goToLogin2()
            },
            String::class.java
        )
        hubConnection.on(
            "JoinedRoom",
            { message: String ->
                joinedRoomName = message
                callback?.showToast("Dołączono do pokoju.")
                callback?.startGameActivity()
            },
            String::class.java
        )
        hubConnection.on(
            "GetChatMessage",
            { message: String ->
                callback?.getChatMessage(message)
            },
            String::class.java
        )
        hubConnection.onClosed {
            connect()
        }
        location = Location(this)
        connect()
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    private fun checkHubConnection(): Boolean {
        return if (hubConnection.connectionState == HubConnectionState.CONNECTED)
            true
        else {
            callback?.showDialog("Brak połączenia", "Ponowne łączenie...")
            //connect()
            //hubConnection.start()
            false
        }
    }

    fun connect() {
        count = 0
        val timerTask = object : TimerTask() {
            override fun run() {
                if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
                    //rejoin to room to actualize ConnectionID on server
                    joinedRoomName?.let { joinJoinedRoom(it) }
                    callback?.hideDialog()
                    timer.cancel()
                    timer.purge()
                } else {
                    if (location.perms.checkInternetConnection()) {
                        hubConnection.start()
                    } else if (count == 0) {
                        callback?.showToast("Uruchom transmisję danych.")
                    }
                    count++
                }
            }
        }
        //turn on timer
        timer = Timer()
        timer.schedule(timerTask, 0, 2000)
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
                    callback?.showDialog("Brak połączenia", "Ponowne łączenie...")
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

    fun createRoom(roomName: String, password: String, team: Int) {
        if (checkHubConnection())
            hubConnection.invoke("CreateRoom", roomName, password, team, token)
    }

    fun joinRoom(roomName: String, password: String, team: Int) {
        if (checkHubConnection()) {
            hubConnection.invoke("JoinRoom", roomName, password, team, token)
        }
    }

    fun joinJoinedRoom(roomName: String) {
        if (checkHubConnection()) {
            hubConnection.invoke("JoinJoinedRoom", roomName, token)
        }
    }

    fun sendMessage(message: String, toAll: Boolean) {
        hubConnection.invoke("SendMessage", message, toAll, token)
    }

}