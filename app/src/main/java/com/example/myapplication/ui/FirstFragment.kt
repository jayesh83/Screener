package com.example.myapplication.ui

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
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.WifiP2pPeersAdapter
import com.example.myapplication.receivers.WiFiDirectBroadcastReceiver
import com.example.myapplication.util.DirectActionListener
import com.example.myapplication.util.States
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton

private const val TAG = "FirstFragment"

class FirstFragment : Fragment() {
    private lateinit var wifiP2pManager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    private var wifiP2pInfo: WifiP2pInfo? = null
    private var wifiP2pEnabled = false
    private var mWifiP2pDevice: WifiP2pDevice? = null
    private val p2pDeviceList: ArrayList<WifiP2pDevice?>? = arrayListOf()
    private lateinit var wiFiDirectBroadcastReceiver: WiFiDirectBroadcastReceiver
    private lateinit var wifiP2pDeviceListAdapter: WifiP2pPeersAdapter
    private lateinit var thisDeviceName: TextView

    // ui
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

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
                this@FirstFragment.wifiP2pInfo = wifiP2pInfo
            }
        }

        override fun onDisconnection() {
            Log.e(TAG, "onDisconnection")
            p2pDeviceList?.clear()
            wifiP2pDeviceListAdapter.notifyDataSetChanged()
            this@FirstFragment.wifiP2pInfo = null
        }

        override fun onSelfDeviceAvailable(wifiP2pDevice: WifiP2pDevice?) {
            Log.e(TAG, "onSelfDeviceAvailable")
            thisDeviceName.text = wifiP2pDevice?.deviceName
            Log.e(TAG, "$TAG Status: ${States.getDeviceStatus(wifiP2pDevice?.status)}")
            Log.e(TAG, "$TAG Device Name: ${wifiP2pDevice?.deviceName}")
            Log.e(TAG, "$TAG Device Address: ${wifiP2pDevice?.deviceAddress}")
        }

        override fun onPeersAvailable(wifiP2pDeviceList: Collection<WifiP2pDevice?>?) {
            Log.e(TAG, "onPeersAvailable :" + wifiP2pDeviceList?.size)
            p2pDeviceList?.clear()
            if (wifiP2pDeviceList != null) {
                p2pDeviceList?.addAll(wifiP2pDeviceList)
                Log.e(TAG, "List: \n${p2pDeviceList?.toString()}")
            }
            wifiP2pDeviceListAdapter.notifyDataSetChanged()
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
        val bottomSheet = view.findViewById<ConstraintLayout>(R.id.layout_bottom_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> toast("Expanded")
                    BottomSheetBehavior.STATE_COLLAPSED -> toast("Collapsed")
                    BottomSheetBehavior.STATE_HIDDEN -> toast("Hidden")
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        requireActivity().findViewById<FloatingActionButton>(R.id.fab).visibility = View.INVISIBLE

        thisDeviceName = view.findViewById(R.id.this_device_name)
        // TODO: This device name is not showing up! Fix it

        val recyclerViewPeersList = view.findViewById<RecyclerView>(R.id.recyclerview_peers_list)
        val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        recyclerViewPeersList.addItemDecoration(divider)
        wifiP2pDeviceListAdapter = WifiP2pPeersAdapter(p2pDeviceList)
        recyclerViewPeersList.adapter = wifiP2pDeviceListAdapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initEvent()
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

    private fun toast(message: String) {
        Toast.makeText(requireContext().applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun discoverPeers(): Boolean {
        if (!wifiP2pEnabled)
            Log.e(TAG, "Wifi Disabled")

        try {
            wifiP2pManager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.e(TAG, "Peer Discovery Success")
                }

                override fun onFailure(reason: Int) {
                    Log.e(
                        TAG,
                        "Peer Discovery Failed - ${States.getDiscoveryFailureReason(reason)}"
                    )
                }
            })
        } catch (e: SecurityException) {
            Log.e(TAG, "Exception: $e")
        }
        return true
    }

    @SuppressLint("MissingPermission")
    private fun connectFirst(): Boolean {
        Log.e(TAG, "Connecting")
        val config = WifiP2pConfig()
        if (config.deviceAddress != null && mWifiP2pDevice != null) {
            config.deviceAddress = mWifiP2pDevice!!.deviceAddress
            config.wps.setup = WpsInfo.PBC
            wifiP2pManager.connect(channel, config, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.e(TAG, "Connected to ${config.deviceAddress}")
                }

                override fun onFailure(reason: Int) {
                    Log.e(TAG, "Connection Failed")
                }
            })
        } else {
            Log.e(TAG, "P2P device: $mWifiP2pDevice")
            Log.e(TAG, "Device Address: ${config.deviceAddress}")
        }
        return true
    }

    @SuppressLint("MissingPermission")
    private fun connectDevice(device: WifiP2pDevice?) {
        Log.e(TAG, "Connecting")
        val config = WifiP2pConfig()
        if (config.deviceAddress != null && device != null) {
            config.deviceAddress = device.deviceAddress
            config.wps.setup = WpsInfo.PBC // responsible for connecting without key/pincode

            wifiP2pManager.connect(channel, config, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.e(TAG, "Connection invitation to ${config.deviceAddress}")
                }

                override fun onFailure(reason: Int) {
                    Log.e(TAG, "Connection Failed")
                }
            })
        } else {
            Log.e(TAG, "Could not connect because P2P device or This Device Network is $device")
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
        requireActivity().menuInflater.inflate(R.menu.menu_fragment_send, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_discover -> discoverPeers()
            R.id.action_connect -> connectFirst()
            else -> super.onOptionsItemSelected(item)
        }
    }

}