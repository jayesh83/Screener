package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context.WIFI_P2P_SERVICE
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.receivers.WiFiDirectBroadcastReceiver
import com.example.myapplication.util.DirectActionListener
import com.example.myapplication.util.States


private const val TAG = "FirstFragment"

class FirstFragment : Fragment() {
    private lateinit var wifiP2pManager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    private var wifiP2pInfo: WifiP2pInfo? = null
    private var wifiP2pEnabled = false
    private val mWifiP2pDevice: WifiP2pDevice? = null
    private val wifiP2pDeviceList: ArrayList<WifiP2pDevice>? = null
    private lateinit var wiFiDirectBroadcastReceiver: WiFiDirectBroadcastReceiver

    init {
        setHasOptionsMenu(true)
    }

    private val directActionListener = object : DirectActionListener,
        WifiP2pManager.ChannelListener {
        override fun wifiP2pEnabled(enabled: Boolean) {
            wifiP2pEnabled = enabled
        }

        override fun onConnectionInfoAvailable(wifiP2pInfo: WifiP2pInfo?) {
            Log.e(TAG, "onConnectionInfoAvailable")
            Log.e(TAG, "$TAG GrpFormed: ${wifiP2pInfo?.groupFormed}")
            Log.e(TAG, "$TAG GroupOwner: ${wifiP2pInfo?.isGroupOwner}")
            Log.e(TAG, "$TAG GroupAddress: ${wifiP2pInfo?.groupOwnerAddress?.hostAddress}")

            if (wifiP2pInfo?.groupFormed == true && !wifiP2pInfo.isGroupOwner) {
                this@FirstFragment.wifiP2pInfo = wifiP2pInfo;
            }
        }

        override fun onDisconnection() {
            Log.e(TAG, "onDisconnection")
            wifiP2pDeviceList?.clear()
            this@FirstFragment.wifiP2pInfo = null
        }

        override fun onSelfDeviceAvailable(wifiP2pDevice: WifiP2pDevice?) {
            Log.e(TAG, "onSelfDeviceAvailable")
            Log.e(TAG, "$TAG Status: ${wifiP2pDevice?.status}")
            Log.e(TAG, "$TAG Device Name: ${wifiP2pDevice?.deviceName}")
            Log.e(TAG, "$TAG Device Address: ${wifiP2pDevice?.deviceAddress}")
        }

        override fun onPeersAvailable(wifiP2pDeviceList: Collection<WifiP2pDevice>?) {
            Log.e(TAG, "onPeersAvailable :" + wifiP2pDeviceList?.size)
            this@FirstFragment.wifiP2pDeviceList?.clear()
            if (wifiP2pDeviceList != null) {
                this@FirstFragment.wifiP2pDeviceList?.addAll(wifiP2pDeviceList)
                wifiP2pDeviceList.forEach { wifiP2pDevice ->
                    Log.e(TAG, "Device Status: ${States.getDeviceStatus(wifiP2pDevice.status)}")
                    Log.e(TAG, "Device Name: ${wifiP2pDevice.deviceName}")
                    Log.e(TAG, "Device Address: ${wifiP2pDevice.deviceAddress}")
                }
            }
        }

        override fun onChannelDisconnected() {
            Log.e(TAG, "Channel Disconnected")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.button_first).setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        view.findViewById<Button>(R.id.button_discover).setOnClickListener {
            discoverPeers()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initEvent()
        Log.e(TAG, "AppContext: ${requireContext().applicationContext}")
        Log.e(TAG, "WifiP2pManager: $wifiP2pManager")
        Log.e(TAG, "WifiP2pChannel: $channel")
        Log.e(TAG, "WifiP2pDirectActionListener: $directActionListener")
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unregisterReceiver(wiFiDirectBroadcastReceiver)
    }

    private fun initEvent() {
        wifiP2pManager = requireContext().getSystemService(WIFI_P2P_SERVICE) as WifiP2pManager
        channel = wifiP2pManager.initialize(
            requireContext(),
            requireActivity().mainLooper,
            directActionListener
        )

        wiFiDirectBroadcastReceiver = WiFiDirectBroadcastReceiver(
            requireContext().applicationContext,
            wifiP2pManager,
            channel,
            directActionListener
        )
        requireContext().registerReceiver(
            wiFiDirectBroadcastReceiver,
            WiFiDirectBroadcastReceiver.getIntentFilter()
        )
    }

    private fun discoverPeers(): Boolean {
        if (!wifiP2pEnabled) {
            Log.e(TAG, "Wifi Disabled")
            return true;
        }
        return try {
            wifiP2pManager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.e(TAG, "Peer Discovery Success")
                }

                override fun onFailure(reason: Int) {
                    Log.e(TAG, "Peer Discovery Failed - $reason")
                }
            })
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "Exception: $e")
            true
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectFirst(): Boolean {
        val config = WifiP2pConfig()
        if (config.deviceAddress != null && mWifiP2pDevice != null) {
            config.deviceAddress = mWifiP2pDevice.deviceAddress
            config.wps.setup = WpsInfo.PBC
            wifiP2pManager.connect(channel, config, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.e(TAG, "Connected to ${config.deviceAddress}")
                }

                override fun onFailure(reason: Int) {
                    Log.e(TAG, "Connection Failed")
                }
            })
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
        requireActivity().menuInflater.inflate(R.menu.menu_fragment_send, menu)
    }

//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_discover -> discoverPeers()
            R.id.action_connect -> connectFirst()
            else -> super.onOptionsItemSelected(item)
        }
    }

}