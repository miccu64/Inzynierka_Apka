package com.example.larp_app.services

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.IBinder
import com.example.larp_app.others.MyPermissions
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import java.util.*


class HubService : Service() {
    companion object {
        private var token: String? = null
        private var timer: Timer = Timer()
        private var callback: IHubCallback? = null
        private var joinedRoomName: String? = null
        private var taskTimer: TimerTask? = null
    }

    // Binder given to clients
    private val binder = HubBinder()
    private var previousLocation: Location? = null
    private val server: String = "http://192.168.0.10:45455"
    //private val server: String = "https://larpserver.herokuapp.com"

    private lateinit var hubConnection: HubConnection
    private val perms = MyPermissions(this)
    private lateinit var location: GPSNetworkLocation

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
                location.stop()
                resetTimer()
                joinedRoomName = null
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
        location = GPSNetworkLocation(this)
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

    fun checkConnectionDialog(): Boolean {
        if (!checkHubConnection()) {
            if (!perms.checkInternetConnection()) {
                callback?.showDialog("Wyłączona transmisja danych", "Uruchom transmisję danych.")
            } else callback?.showDialog("Brak połączenia", "Ponowne łączenie...")
        }
        return checkHubConnection()
    }

    private fun connect() {
        resetTimer()
        checkConnectionDialog()

        taskTimer = object : TimerTask() {
            override fun run() {
                if (checkHubConnection()) {
                    callback?.hideDialog()
                    //rejoin to room to actualize ConnectionID on server if were in game
                    if (joinedRoomName != null) {
                        joinJoinedRoom(joinedRoomName!!, true)
                        //send location again
                        sendLocation()
                    } else resetTimer() //end reconnect task
                } else if (perms.checkInternetConnection()) {
                    hubConnection.start()
                }
            }
        }
        timer.schedule(taskTimer, 1000, 2000)
    }

    private fun sendLocation() {
        resetTimer()
        taskTimer = object : TimerTask() {
            override fun run() {
                if (checkConnectionDialog()) {
                    val loc = location.getLocation()
                    if (loc != null) {
                        if (previousLocation == null) {
                            hubConnection.invoke("UpdateLocation", loc.latitude, loc.longitude, token)
                        } else if (previousLocation!!.latitude != loc.latitude
                            || previousLocation!!.longitude != loc.longitude) {
                            hubConnection.invoke("UpdateLocation", loc.latitude, loc.longitude, token)
                        }
                        previousLocation = loc
                    } else callback?.showToast("Musisz uruchomić lokalizację")
                }
            }
        }
        timer.schedule(taskTimer, 1000, 2000)
    }

    fun register(email: String, name: String, password: String) {
        if (checkConnectionDialog()) {
            callback?.showDialog("", "Rejestrowanie...")
            hubConnection.invoke("RegisterNewUser", email, name, password)
        }
    }

    fun login(email: String, password: String) {
        if (checkConnectionDialog()) {
            callback?.showDialog("", "Logowanie...")
            hubConnection.invoke("Login", email, password)
        }
    }

    fun createRoom(roomName: String, password: String, team: Int) {
        if (checkConnectionDialog()) {
            callback?.showDialog("", "Tworzenie pokoju...")
            hubConnection.invoke("CreateRoom", roomName, password, team, token)
        }
    }

    fun joinRoom(roomName: String, password: String, team: Int) {
        if (checkConnectionDialog()) {
            callback?.showDialog("", "Dołączanie do pokoju...")
            hubConnection.invoke("JoinRoom", roomName, password, team, token)
        }
    }

    fun joinJoinedRoom(roomName: String, lostConnection: Boolean) {
        if (checkConnectionDialog()) {
            if (!lostConnection) {
                callback?.showDialog("", "Dołączanie do pokoju...")
            }
            hubConnection.invoke("JoinJoinedRoom", roomName, lostConnection, token)
        }
    }

    fun sendMessage(message: String, toAll: Boolean) {
        if (checkConnectionDialog())
            hubConnection.invoke("SendMessage", message, joinedRoomName, toAll, token)
    }

    fun giveAdmin(nick: String) {
        if (checkConnectionDialog())
            hubConnection.invoke("GiveAdmin", joinedRoomName, nick, token)
    }

    fun throwPlayer(nick: String) {
        if (checkConnectionDialog())
            hubConnection.invoke("ThrowPlayer", joinedRoomName, nick, token)
    }

    fun leaveRoom() {
        if (checkConnectionDialog())
            hubConnection.invoke("LeaveRoom", joinedRoomName, token)
    }

    fun deleteRoom() {
        if (checkConnectionDialog())
            hubConnection.invoke("DeleteRoom", joinedRoomName, token)
    }
}