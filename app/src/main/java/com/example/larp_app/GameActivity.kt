package com.example.larp_app

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.example.larp_app.services.HubService
import com.example.larp_app.services.IHubCallback
import com.example.larp_app.ui.room.ChatFragment
import com.example.larp_app.ui.room.MapsFragment
import com.example.larp_app.ui.room.SectionsPagerAdapter
import com.google.android.material.tabs.TabLayout


class GameActivity : IHubCallback, AppCompatActivity() {

    private lateinit var dialog: android.app.AlertDialog



    lateinit var hub: HubService
    private var bound: Boolean = false

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as HubService.HubBinder
            hub = binder.getService()
            hub.setCallbacks(this@GameActivity)
            bound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bound = false
        }
    }

    override fun onStart() {
        super.onStart()

        // Bind to LocalService
        Intent(this, HubService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (bound) {
            hub.setCallbacks(null)
            unbindService(connection)
            bound = false
        }
    }

    override fun showToast(text: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this, text, Toast.LENGTH_LONG).show()
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
        try {
            dialog.dismiss()
        } catch (e: Exception) { }
    }

    override fun loginSuccess(roomList: String) { }

    override fun goToLogin2() { }

    override fun startGameActivity() { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_game)
        val sectionsPagerAdapter =
            SectionsPagerAdapter(
                this,
                supportFragmentManager
            )
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
    }

    fun sendMessage(message: String, toAll: Boolean) {
        hub.sendMessage(message, toAll)
    }

    override fun getChatMessage(message: String) {
        val fragment: ChatFragment = supportFragmentManager.fragments[1] as ChatFragment
        fragment.getChatMessage(message)
    }

    override fun showOnMap(coords: String) {
        val fragment: MapsFragment = supportFragmentManager.fragments[0] as MapsFragment
        fragment.showOnMap(coords)
    }

}