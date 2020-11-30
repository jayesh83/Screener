package com.example.myapplication.receivers

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.myapplication.util.DirectActionListener
import com.example.myapplication.util.States

private const val TAG = "WifiP2pStatusReceiver"

class WiFiDirectBroadcastReceiver(
    private val appContext: Context,
    private val p2pManager: WifiP2pManager,
    private val channel: WifiP2pManager.Channel,
    private val directActionListener: DirectActionListener
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.e(TAG, "AppContext: $appContext")
        Log.e(TAG, "WifiP2pManager: $p2pManager")
        Log.e(TAG, "WifiP2pChannel: $channel")
        Log.e(TAG, "WifiP2pDirectActionListener: $directActionListener")
        when (intent?.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                States.isWifiP2pEnabled = state == WifiP2pManager.WIFI_P2P_STATE_ENABLED
                directActionListener.wifiP2pEnabled(States.isWifiP2pEnabled)
                val wifiP2pDeviceList = arrayListOf<WifiP2pDevice>()
                directActionListener.onPeersAvailable(wifiP2pDeviceList)
                Log.e(TAG, "Wifi P2P Enabled: ${States.isWifiP2pEnabled}")
            }

            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                Log.e(TAG, "Peers changed")
                if (ActivityCompat.checkSelfPermission(
                        appContext,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.e(TAG, "Location permission missing")
                    return
                }

                p2pManager.requestPeers(channel) { peers ->
                    directActionListener.onPeersAvailable(peers?.deviceList)
                }
            }

            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                Log.e(TAG, "Connection State Change")
                val wifiInfo =
                    intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO) as WifiP2pInfo?

                val networkInfo: NetworkInfo? =
                    intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO)

                if (networkInfo != null && networkInfo.isConnected()) {
                    Log.e(TAG, "Requesting Connection Info")
                    p2pManager.requestConnectionInfo(channel) { info ->
                        Log.e(TAG, "Connection Info Available")
                        directActionListener.onConnectionInfoAvailable(info)
                    }
                } else {
                    directActionListener.onDisconnection();
                    Log.e(TAG, "Disconnected from P2P Device");
                }

//                Log.e(
//                    TAG, "GroupOwner: ${wifiInfo?.isGroupOwner}\n" +
//                            "GroupOwnerAddress: ${wifiInfo?.groupOwnerAddress}" +
//                            "\nGroupFormed: ${wifiInfo?.groupFormed}"
//                )
            }

            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                Log.e(TAG, "Device Info Changed")
                val device =
                    intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE) as WifiP2pDevice?
                directActionListener.onSelfDeviceAvailable(device)
//                Log.e(
//                    TAG, "Name: ${device?.deviceName}\n" +
//                            "Address: ${device?.deviceAddress}\n" +
//                            "Status: ${device?.status}"
//                )
            }
        }
    }
    companion object{
        fun getIntentFilter(): IntentFilter {
            return IntentFilter().apply {
                // Indicates a change in the Wi-Fi P2P status.
                addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)

                // Indicates a change in the list of available peers.
                addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)

                // Indicates the state of Wi-Fi P2P connectivity has changed.
                addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)

                // Indicates this device's details have changed.
                addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
            }
        }
    }
}