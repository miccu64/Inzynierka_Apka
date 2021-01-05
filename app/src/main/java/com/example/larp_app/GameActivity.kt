package com.example.larp_app

import android.app.AlertDialog
import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
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

    private lateinit var dialog: AlertDialog
    private var newActivity: Boolean = false

    private lateinit var hub: HubService
    private var bound: Boolean = false

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as HubService.HubBinder
            hub = binder.getService()
            hub.setCallbacks(this@GameActivity)
            bound = true
            hub.checkConnectionDialog()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bound = false
        }
    }

    override fun onStart() {
        // Bind to LocalService
        Intent(this, HubService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        super.onStart()
    }

    override fun onDestroy() {
        if (bound) {
            //end sending location
            hub.resetTimer()
            unbindService(connection)
            if (!newActivity) {
                hub.setCallbacks(null)
            }
            bound = false
        }
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_gameactivity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_giveadmin -> {
            val builder = AlertDialog.Builder(this).setTitle("Przekaż uprawnienia (jeśli dotyczy)")
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input).setPositiveButton("OK") { _, _ ->
                if (input.text.toString().isNotEmpty())
                    hub.giveAdmin(input.text.toString())
            }
            builder.setNegativeButton("Anuluj") { dialog, _ -> dialog.cancel() }.show()
            true
        }
        R.id.action_leave -> {
            hub.leaveRoom()
            true
        }
        R.id.action_return -> {
            goToLogin()
            true
        }
        R.id.action_delete -> {
            AlertDialog.Builder(this)
                .setTitle("Usunąć pokój? (jeśli jesteś adminem)")
                .setPositiveButton("Tak") { _, _ ->
                    hub.deleteRoom()
                }
                .setNegativeButton("Nie", null)
                .show()
            true
        }
        R.id.action_throw -> {
            val builder = AlertDialog.Builder(this).setTitle("Wyrzuć gracza (jeśli jesteś adminem)")
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input).setPositiveButton("OK") { _, _ ->
                if (input.text.toString().isNotEmpty())
                    hub.throwPlayer(input.text.toString())
            }
            builder.setNegativeButton("Anuluj") { dialog, _ -> dialog.cancel() }.show()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
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
            val builder = AlertDialog.Builder(this)
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

    override fun goToLogin() {
        //needed creation of new GameActivity in onDestroy()
        newActivity = true
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        //end current activity
        this.finish()
    }

    override fun startGameActivity() { }

    override fun onCreate(savedInstanceState: Bundle?) {
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
        super.onCreate(savedInstanceState)
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

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Wyjście z aplikacji")
            .setMessage("Na pewno chcesz zakończyć działanie aplikacji?")
            .setPositiveButton("Tak") { _, _ ->
                val myService = Intent(this@GameActivity, HubService::class.java)
                hub.stopService(myService)
                finish()
            }
            .setNegativeButton("Nie", null)
            .show()
    }
}