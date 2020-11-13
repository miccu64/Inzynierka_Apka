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
import androidx.fragment.app.FragmentTransaction
import com.example.inzynierka_apka.others.MyPermissions
import com.example.inzynierka_apka.services.HubService
import com.example.inzynierka_apka.ui.login.LoginFragment
import com.example.inzynierka_apka.ui.login.RegisterFragment


class MainActivity : AppCompatActivity() {
    //HTTPS nie zadziala na localhoscie - moze na zewnatrz pojdzie? pasobaloby xD
    //w AndroidManifest.xml jest dodana linia i moze nie dzialac cos przez nia
    private lateinit var perms: MyPermissions

    lateinit var hub: HubService

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as HubService.HubBinder
            hub = binder.getService()
        }

        override fun onServiceDisconnected(arg0: ComponentName) { }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Begin the transaction
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, LoginFragment())
        ft.commit()

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

    fun loginButton(view: View) {
        val login = findViewById<TextView>(R.id.emailLogin).text.toString()
        val pass = findViewById<TextView>(R.id.passwordLogin).text.toString()
        hub.login(login, pass)
    }

    fun registerButton(view: View) {
        val login = findViewById<TextView>(R.id.loginRegister).text.toString()
        val email = findViewById<TextView>(R.id.emailRegister).text.toString()
        val pass = findViewById<TextView>(R.id.passwordRegister).text.toString()
        hub.register(email, login, pass)
    }

    fun goToRegister(view: View) {
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, RegisterFragment())
        ft.commit()
    }

    fun goToLogin(view: View) {
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, LoginFragment())
        ft.commit()
    }

}