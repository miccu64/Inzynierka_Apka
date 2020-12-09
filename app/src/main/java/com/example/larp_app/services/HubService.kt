package com.example.larp_app.services

import LocationService
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import java.util.*


class HubService : Service() {
    var token: String? = null

    // Binder given to clients
    private val binder = HubBinder()
    private var timer: Timer = Timer()
    private var callback: IHubCallback? = null
    private var joinedRoomName: String? = null
    private var taskTimer: TimerTask? = null


    //private val server: String = "http://192.168.0.10:45455"

    private val server: String = "https://larpserver.herokuapp.com"
    private lateinit var hubConnection: HubConnection
    private lateinit var location: LocationService

    inner class HubBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): HubService = this@HubService
    }

    fun setCallbacks(call: IHubCallback?) {
        callback = call
    }

    override fun onUnbind(intent: Intent?): Boolean {
        resetTimer()
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        super.onCreate()

        hubConnection = HubConnectionBuilder.create("$server/gamehub").build()
        //functions invoked from server
        hubConnection.on(
            "ShowMessage",
            { message: String ->
                callback?.showToast(message)
                callback?.hideDialog()
            }, String::class.java
        )
        hubConnection.on(
            "RegisterSuccess",
            { message: String ->
                callback?.showDialog(message, "Możesz się zalogować")
                callback?.goToLogin()
            }, String::class.java
        )
        hubConnection.on(
            "GetLocationFromServer",
            { message: String ->
                callback?.showOnMap(message)
            },
            String::class.java
        )
        hubConnection.on(
            "LoginSuccess",
            { tok: String, roomList: String ->
                token = tok
                callback?.hideDialog()
                callback?.loginSuccess(roomList)
            },
            String::class.java, String::class.java
        )
        hubConnection.on(
            "GoToLogin",
            { message: String ->
                callback?.showToast(message)
                callback?.goToLogin()
            },
            String::class.java
        )
        hubConnection.on(
            "JoinedRoom",
            { message: String ->
                joinedRoomName = message
                callback?.showToast("Dołączono do pokoju.")
                callback?.startGameActivity()
                sendLocation()
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
        location = LocationService(this)
        connect()
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    private fun checkHubConnection(): Boolean {
        return hubConnection.connectionState == HubConnectionState.CONNECTED
    }

    fun resetTimer() {
        taskTimer?.cancel()
        timer.purge()
        timer.cancel()
        timer = Timer()
    }

    private fun connect() {
        resetTimer()
        taskTimer = object : TimerTask() {
            var count = 0
            override fun run() {
                if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
                    //rejoin to room to actualize ConnectionID on server
                    if (joinedRoomName != null) {
                        joinJoinedRoom(joinedRoomName!!, true)
                    }
                    callback?.hideDialog()
                    //send location again if in game
                    if (joinedRoomName != null)
                        sendLocation()
                    //end reconnect task
                    else resetTimer()
                } else {
                    if (location.perms.checkInternetConnection()) {
                        hubConnection.start()
                    } else if (count == 2)
                        callback?.showToast("Uruchom transmisję danych.")
                    if (count == 2)
                        callback?.showDialog("Brak połączenia", "Ponowne łączenie...")
                    count++
                }
            }
        }
        timer.schedule(taskTimer, 0, 2000)
    }

    private fun sendLocation() {
        resetTimer()
        taskTimer = object : TimerTask() {
            override fun run() {
                if (checkHubConnection()) {
                    val lat = LocationService.latitude
                    val lon = LocationService.longitude
                    if (lat == lon && lat == 0.0)
                        callback?.showToast("Nie można zdobyć lokalizacji")
                    else hubConnection.invoke("UpdateLocation", lat, lon, token)
                }
                else callback?.showDialog("Brak połączenia", "Ponowne łączenie...")
            }
        }
        timer.schedule(taskTimer, 0, 2000)
    }

    fun register(email: String, name: String, password: String) {
        if (checkHubConnection()) {
            callback?.showDialog("", "Rejestrowanie...")
            hubConnection.invoke("RegisterNewUser", email, name, password)
        }
    }

    fun login(email: String, password: String) {
        if (checkHubConnection()) {
            callback?.showDialog("", "Logowanie...")
            hubConnection.invoke("Login", email, password)
        }
    }

    fun createRoom(roomName: String, password: String, team: Int) {
        if (checkHubConnection()) {
            callback?.showDialog("", "Tworzenie pokoju...")
            hubConnection.invoke("CreateRoom", roomName, password, team, token)
        }
    }

    fun joinRoom(roomName: String, password: String, team: Int) {
        if (checkHubConnection()) {
            callback?.showDialog("", "Dołączanie do pokoju...")
            hubConnection.invoke("JoinRoom", roomName, password, team, token)
        }
    }

    fun joinJoinedRoom(roomName: String, lostConnection: Boolean) {
        if (checkHubConnection()) {
            if (!lostConnection) {
                callback?.showDialog("", "Dołączanie do pokoju...")
            }
            hubConnection.invoke("JoinJoinedRoom", roomName, lostConnection, token)
        }
    }

    fun sendMessage(message: String, toAll: Boolean) {
        if (checkHubConnection())
            hubConnection.invoke("SendMessage", message, joinedRoomName, toAll, token)
    }

    fun giveAdmin(nick: String) {
        if (checkHubConnection())
            hubConnection.invoke("GiveAdmin", joinedRoomName, nick, token)
    }

    fun throwPlayer(nick: String) {
        if (checkHubConnection())
            hubConnection.invoke("ThrowPlayer", joinedRoomName, nick, token)
    }

    fun leaveRoom() {
        if (checkHubConnection())
            hubConnection.invoke("LeaveRoom", joinedRoomName, token)
    }

    fun deleteRoom() {
        if (checkHubConnection())
            hubConnection.invoke("DeleteRoom", joinedRoomName, token)
    }
}