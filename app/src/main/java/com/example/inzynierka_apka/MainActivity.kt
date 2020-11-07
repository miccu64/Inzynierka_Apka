package com.example.inzynierka_apka

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.example.inzynierka_apka.others.MyPermissions
import com.example.inzynierka_apka.services.HubService
import java.util.*
import kotlin.concurrent.timerTask

class MainActivity : AppCompatActivity() {
    private var textView: TextView? = null
    //HTTPS nie zadziala na localhoscie - moze na zewnatrz pojdzie? pasobaloby xD
    //w AndroidManifest.xml jest dodana linia i moze nie dzialac cos przez nia
    private lateinit var perms: MyPermissions

    private lateinit var hub: HubService
    private var mBound: Boolean = false

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as HubService.HubBinder
            hub = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //ignores every certificate of SSL!!!!!!!!!!!!!!
        NukeSSLCerts.nuke()

        perms = MyPermissions(this)
        grantPermissions() //exitProcess(-1)

        // Bind to LocalService
        Intent(this, HubService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
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

    fun showLocalization(view: View) {
        hub.connect()

        //hub.register("nowy2", "nowy2", "nowy2")
        hub.login("nowy2", "nowy2")
        Timer().schedule(timerTask {
            hub.createRoom("idz3", "idz3")
            hub.joinRoom("idz3", "idz3")
        }, 1000)
    }

    fun loginButton(view: View) {
        val login = findViewById<TextView>(R.id.inputLogin).text.toString()
        val pass = findViewById<TextView>(R.id.inputPassword).text.toString()
        hub.login(login, pass)
    }

}