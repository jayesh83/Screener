package com.example.myapplication.ui

import android.annotation.SuppressLint
import android.content.Context.WIFI_P2P_SERVICE
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.WifiP2pPeersAdapter
import com.example.myapplication.receivers.WiFiDirectBroadcastReceiver
import com.example.myapplication.ui.model.P2pDevice
import com.example.myapplication.ui.viewmodel.FirstFragmentViewModel
import com.example.myapplication.util.DirectActionListener
import com.example.myapplication.util.States
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import timber.log.Timber

private const val TAG = "FirstFragment"

class FirstFragment : Fragment() {
    private lateinit var wifiP2pManager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    private var wifiP2pInfo: WifiP2pInfo? = null
    private var wifiP2pEnabled = false
    private var mWifiP2pDevice: WifiP2pDevice? = null
    private lateinit var wiFiDirectBroadcastReceiver: WiFiDirectBroadcastReceiver
    private var wifiP2pDeviceListAdapter: WifiP2pPeersAdapter? = null
    private lateinit var thisDeviceName: TextView

    // ui
    private val viewModel: FirstFragmentViewModel by viewModels()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    init {
        setHasOptionsMenu(true)
    }

    private val directActionListener = viewModel.directActionListener
/*    private val directActionListener =
        object : DirectActionListener, WifiP2pManager.ChannelListener {

            override fun wifiP2pEnabled(enabled: Boolean) {
                wifiP2pEnabled = enabled
            }

            override fun onConnectionInfoAvailable(wifiP2pInfo: WifiP2pInfo) {
                Timber.i("onConnectionInfoAvailable")
                Timber.i("$TAG GrpFormed: ${wifiP2pInfo?.groupFormed}")
                Timber.i("$TAG GroupOwner: ${wifiP2pInfo?.isGroupOwner}")
                Timber.i("$TAG GroupAddress: ${wifiP2pInfo?.groupOwnerAddress?.hostAddress}")

                if (wifiP2pInfo.groupFormed && !wifiP2pInfo.isGroupOwner) {
                    this@FirstFragment.wifiP2pInfo = wifiP2pInfo
                }
            }

            override fun onDisconnection() {
                Timber.i("onDisconnection")
                p2pDeviceList?.clear()
                wifiP2pDeviceListAdapter.notifyDataSetChanged()
                this@FirstFragment.wifiP2pInfo = null
            }

            override fun onSelfDeviceAvailable(wifiP2pDevice: WifiP2pDevice) {
                Timber.i("onSelfDeviceAvailable")
                thisDeviceName.text = wifiP2pDevice.deviceName
                Timber.i("$TAG Status: ${States.getDeviceStatus(wifiP2pDevice.status)}")
                Timber.i("$TAG Device Name: ${wifiP2pDevice.deviceName}")
                Timber.i("$TAG Device Address: ${wifiP2pDevice.deviceAddress}")
            }

            override fun onPeersAvailable(wifiP2pDeviceList: Collection<WifiP2pDevice>) {
                Timber.i("onPeersAvailable : + ${wifiP2pDeviceList.size}")
                p2pDeviceList?.clear()
                p2pDeviceList?.addAll(wifiP2pDeviceList)
                Timber.i("List: \n${p2pDeviceList?.toString()}")
                wifiP2pDeviceListAdapter.notifyDataSetChanged()
            }

            override fun onChannelDisconnected() {
                Timber.i("Channel Disconnected")
            }
        }*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // don't need floating button in fragment
        requireActivity().findViewById<FloatingActionButton>(R.id.fab).visibility = View.INVISIBLE
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = WifiP2pPeersAdapter { device -> adapterOnClick(device) }
        val peersListRecyclerView = view.findViewById<RecyclerView>(R.id.recyclerview_peers_list)
        peersListRecyclerView.adapter = adapter

        viewModel.onPeersAvailable.observe(viewLifecycleOwner, { collectionP2pDevice ->

        })

        thisDeviceName = view.findViewById(R.id.this_device_name)
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


        // TODO: This device name is not showing up! Fix it

        val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.HORIZONTAL)
        recyclerViewPeersList.addItemDecoration(divider)
    }

    private fun adapterOnClick(device: WifiP2pDevice) {
        connectDevice(device)
    }

    private fun addDevicesToList(collectionP2pDevice: Collection<WifiP2pDevice>) {
        val updated =
            wifiP2pDeviceListAdapter?.addDevicesToList(collectionP2pDevice as ArrayList<WifiP2pDevice>)
        if (updated == true)
            wifiP2pDeviceListAdapter?.notifyDataSetChanged()
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
            directActionListener as WifiP2pManager.ChannelListener?
        )

        wiFiDirectBroadcastReceiver = WiFiDirectBroadcastReceiver(
            requireContext().applicationContext,
            wifiP2pManager,
            channel,
            directActionListener as DirectActionListener
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
        if (viewModel.isWifiP2pEnabled.value == false)
            Timber.i("Wifi Disabled")

        try {
            wifiP2pManager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Timber.i("Peer Discovery Success")
                }

                override fun onFailure(reason: Int) {
                    Timber.e("Peer Discovery Failed - ${States.getDiscoveryFailureReason(reason)}")
                }
            })
        } catch (e: SecurityException) {
            Timber.i("Exception: $e")
        }
        return true
    }

//    @SuppressLint("MissingPermission")
//    private fun connectFirst(): Boolean {
//        Timber.i("Connecting")
//        val config = WifiP2pConfig()
//        if (config.deviceAddress != null && mWifiP2pDevice != null) {
//            config.deviceAddress = mWifiP2pDevice!!.deviceAddress
//            config.wps.setup = WpsInfo.PBC
//            wifiP2pManager.connect(channel, config, object : WifiP2pManager.ActionListener {
//                override fun onSuccess() {
//                    Timber.i("Connected to ${config.deviceAddress}")
//                }
//
//                override fun onFailure(reason: Int) {
//                    Timber.i("Connection Failed")
//                }
//            })
//        } else {
//            Timber.i("P2P device: $mWifiP2pDevice")
//            Timber.i("Device Address: ${config.deviceAddress}")
//        }
//        return true
//    }

    @SuppressLint("MissingPermission")
    private fun connectDevice(device: WifiP2pDevice?) {
        Timber.i("Connecting")
        val config = WifiP2pConfig()
        if (config.deviceAddress != null && device != null) {
            config.deviceAddress = device.deviceAddress
            config.wps.setup = WpsInfo.PBC // responsible for connecting without key/pincode

            wifiP2pManager.connect(channel, config, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Timber.i("Connection invitation to ${config.deviceAddress}")
                }

                override fun onFailure(reason: Int) {
                    Timber.i("Connection Failed")
                }
            })
        } else {
            Timber.i("Could not connect because P2P device or This Device Network is $device")
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
        requireActivity().menuInflater.inflate(R.menu.menu_fragment_send, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_discover -> discoverPeers()
            else -> super.onOptionsItemSelected(item)
        }
    }

}