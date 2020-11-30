package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
//    private val peers = mutableListOf<WifiP2pDevice>()
//    private val intentFilter = IntentFilter()
//    private lateinit var receiver: BroadcastReceiver
//    private lateinit var channel: WifiP2pManager.Channel
//    private lateinit var manager: WifiP2pManager
//    private lateinit var peerListListener: WifiP2pManager.PeerListListener

    private val permissions = arrayOf(
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION
    )


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CHANGE_WIFI_STATE
            ) != PERMISSION_GRANTED
        )
            requestPermissions(permissions, 101)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_WIFI_STATE
            ) != PERMISSION_GRANTED
        )
            requestPermissions(permissions, 102)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PERMISSION_GRANTED
        )
            requestPermissions(permissions, 103)

//        // Indicates a change in the Wi-Fi P2P status.
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
//
//        // Indicates a change in the list of available peers.
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
//
//        // Indicates the state of Wi-Fi P2P connectivity has changed.
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
//
//        // Indicates this device's details have changed.
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
//
//        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
//        channel = manager.initialize(this, mainLooper, null)

//        buttonDiscover.setOnClickListener { thisButton ->
//            // Start discovering peers
//            thisButton.isActivated = false
//            manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
//                override fun onSuccess() {
//                    Log.e(TAG, "Peer discovery success")
//                    thisButton.isActivated = true
//                }
//
//                override fun onFailure(reason: Int) {
//                    thisButton.isActivated = true
//                    Log.e(TAG, "Peer discovery failed- $reason")
//                }
//            })
//        }

//        peerListListener = WifiP2pManager.PeerListListener { peerList ->
//            Log.e(TAG, "Peers changed listener")
//            val refreshedPeers = peerList.deviceList
//            if (refreshedPeers != peers) {
//                peers.clear()
//                peers.addAll(refreshedPeers)
//
////                // If an AdapterView is backed by this data, notify it
////                // of the change. For instance, if you have a ListView of
////                // available peers, trigger an update.
////                (listAdapter as WiFiPeerListAdapter).notifyDataSetChanged()
//
//                // Perform any other updates needed based on the new list of
//                // peers connected to the Wi-Fi P2P network.
//            }
//
////            refreshedPeers.forEach { p2pDevice ->
////                Log.e(TAG, "Device - $p2pDevice")
////            }
//
//            if (peers.isEmpty()) {
//                Log.d(TAG, "No devices found")
//                return@PeerListListener
//            }
//            if (peers.size > 0) {
//                val device = peers[0]
//
//                val config = WifiP2pConfig().apply {
//                    deviceAddress = device.deviceAddress
//                    wps.setup = WpsInfo.PBC
//                }
//
//                manager.connect(channel, config, object : WifiP2pManager.ActionListener {
//
//                    override fun onSuccess() {
//                        // WiFiDirectBroadcastReceiver notifies us. Ignore for now.
//                    }
//
//                    override fun onFailure(reason: Int) {
//                        Toast.makeText(
//                            this@MainActivity,
//                            "Connect failed. Retry.",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                })
//            }
//        }

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
//
//        receiver =
//            WiFiDirectBroadcastReceiver(applicationContext, manager, channel, peerListListener)
//        registerReceiver(receiver, intentFilter)
    }

//    public override fun onResume() {
//        super.onResume()
//        receiver =
//            WiFiDirectBroadcastReceiver(applicationContext, manager, channel, peerListListener)
//        registerReceiver(receiver, intentFilter)
//    }

//    public override fun onPause() {
//        super.onPause()
//        unregisterReceiver(receiver)
//    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}