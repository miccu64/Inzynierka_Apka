package com.example.larp_app

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentTransaction
import com.example.larp_app.others.MyPermissions
import com.example.larp_app.services.HubService
import com.example.larp_app.services.IHubCallback
import com.example.larp_app.ui.login.LoginFragment
import com.example.larp_app.ui.login.RegisterFragment
import com.example.larp_app.ui.room.RoomFragment


class MainActivity : IHubCallback, AppCompatActivity() {
    //HTTPS nie zadziala na localhoscie - moze na zewnatrz pojdzie? pasobaloby xD
    //w AndroidManifest.xml jest dodana linia i moze nie dzialac cos przez nia
    private lateinit var perms: MyPermissions
    private lateinit var dialog: android.app.AlertDialog

    lateinit var hub: HubService
    private var bound: Boolean = false
    private var newActivity: Boolean = false

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as HubService.HubBinder
            hub = binder.getService()
            //register this for callbacks from HubService to fragments
            hub.setCallbacks(this@MainActivity)
            bound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //ignores every certificate of SSL!!!!!!!!!!!!!!
        //NukeSSLCerts.nuke()

        perms = MyPermissions(this)
        grantPermissions() //exitProcess(-1)

    }

    override fun onStart() {
        super.onStart()

        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        //addToBackStack() allows to use back button to back to login from register
        ft.replace(R.id.fragment_container, LoginFragment()).addToBackStack(null)
        ft.commit()

        // Bind to LocalService
        Intent(this, HubService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    fun joinJoinedRoom(room: String) {
        hub.joinJoinedRoom(room, false)
    }

    fun createRoom(name: String, pass: String, team: Int) {
        hub.createRoom(name, pass, team)
    }

    fun joinRoom(name:String, pass: String, team: Int) {
        hub.joinRoom(name, pass, team)
    }

    override fun startGameActivity() {
        //needed creation of new GameActivity in onDestroy()
        newActivity = true
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
        //end current activity
        this.finish()
    }

    override fun onStop() {
        super.onStop()
        if (bound && !newActivity) {
            hub.setCallbacks(null)
            unbindService(connection)
            bound = false
        }
    }

    private fun grantPermissions(): Boolean {
        //checks permissions and ask for them if needed
        if (!perms.checkPermissions())
            ActivityCompat.requestPermissions(
                this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE), 1337)
        else return true
        return perms.checkPermissions()
    }

    fun login(login: String, pass: String) {
        hub.login(login, pass)
    }

    fun register(login: String, email: String, pass: String) {
        hub.register(email, login, pass)
    }

    fun goToRegister(view: View) {
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        //addToBackStack() allows to use back button to back to login from register
        ft.replace(R.id.fragment_container, RegisterFragment()).addToBackStack(null)
        ft.commit()
    }

    fun goToLogin(view: View) {
        //go back to login
        onBackPressed()
    }

    override fun goToLogin2() {
        Handler(Looper.getMainLooper()).post {
            onBackPressed()
        }
    }

    override fun showToast(text: String) {
        //looper is needed bcs of asynchronous callback
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this, text, Toast.LENGTH_LONG).show()
        }
    }

    override fun loginSuccess(roomList: String) {
        hideDialog()
        Handler(Looper.getMainLooper()).post {
            val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
            //addToBackStack() allows to use back button to back to login from register
            ft.replace(R.id.fragment_container,
                RoomFragment(roomList)
            ).addToBackStack(null)
            ft.commit()
        }
    }

    override fun showDialog(title: String, message: String) {
        Handler(Looper.getMainLooper()).post {
            hideDialog()
            val builder = android.app.AlertDialog.Builder(this)
            builder.setCancelable(true).setTitle(title).setMessage(message)
            dialog = builder.create()
            dialog.setCancelable(true)
            dialog.show()
        }
    }

    override fun hideDialog() {
        //try for not dismiss not initialized lateinit property
        try {
            dialog.dismiss()
        } catch (e: Exception) { }
    }

    override fun getChatMessage(message: String) { }

    override fun showOnMap(coords: String) { }

}