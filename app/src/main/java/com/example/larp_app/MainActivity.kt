package com.example.larp_app

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentTransaction
import com.example.larp_app.others.MyPermissions
import com.example.larp_app.services.HubService
import com.example.larp_app.services.IHubCallback
import com.example.larp_app.ui.login.LoginFragment
import com.example.larp_app.ui.login.RegisterFragment


class MainActivity : IHubCallback, AppCompatActivity() {
    //HTTPS nie zadziala na localhoscie - moze na zewnatrz pojdzie? pasobaloby xD
    //w AndroidManifest.xml jest dodana linia i moze nie dzialac cos przez nia
    private lateinit var perms: MyPermissions
    private lateinit var dialog: android.app.AlertDialog

    lateinit var hub: HubService

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as HubService.HubBinder
            hub = binder.getService()
            //register this for callbacks from HubService to fragments
            hub.setCallbacks(this@MainActivity)
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
        onBackPressed()
    }

    override fun loginSuccess() {
        Log.d("TAG", "DFZIALAAAA")
    }

    override fun loginRegisterError(text: String) {
        val loadingBar = findViewById<ProgressBar>(R.id.loading)
        loadingBar.visibility = View.GONE
    }

    override fun showDialog(title: String, message: String) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle(title)
        builder.setMessage(message)
        dialog = builder.create()
        dialog.show()
    }

    override fun hideDialog() {
        //try for not dismiss not initialized lateinit property
        try {
            dialog.dismiss()
            val loadingBar = findViewById<ProgressBar>(R.id.loading)
            loadingBar.visibility = View.GONE
        } catch (e: Exception) { }
    }
}