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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.WifiP2pPeersAdapter
import com.example.myapplication.receivers.WiFiDirectBroadcastReceiver
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

    private lateinit var directActionListener: DirectActionListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // don't need floating button in fragment
        requireActivity().findViewById<FloatingActionButton>(R.id.fab).visibility = View.INVISIBLE
        directActionListener = viewModel.directActionListener as DirectActionListener
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = WifiP2pPeersAdapter { device -> adapterOnClick(device) }
        val peersListRecyclerView = view.findViewById<RecyclerView>(R.id.recyclerview_peers_list)
        peersListRecyclerView.adapter = adapter
        val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.HORIZONTAL)
        peersListRecyclerView.addItemDecoration(divider)

        viewModel.onPeersAvailable.observe(viewLifecycleOwner, { collectionP2pDevice ->
            adapter.submitList(collectionP2pDevice.toMutableList())
        })

        thisDeviceName = view.findViewById(R.id.this_device_name)
        viewModel.onSelfDeviceAvailable.observe(viewLifecycleOwner, { thisWifiDevice ->
            thisWifiDevice?.deviceName?.let {
                thisDeviceName.text = it
            }
        })

        val bottomSheet = view.findViewById<ConstraintLayout>(R.id.layout_bottom_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        bottomSheetBehavior.addBottomSheetCallback(
            object : BottomSheetBehavior.BottomSheetCallback() {

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_EXPANDED) discoverPeers()
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initEvent()
    }

    override fun onDestroy() {
        super.onDestroy()
        wiFiDirectBroadcastReceiver.let {
            requireContext().unregisterReceiver(it)
        }
    }

//    private fun Collection<WifiP2pDevice>.toArrayList(): MutableList<WifiP2pDevice> {
//        val list = mutableListOf<WifiP2pDevice>()
//        this.forEach { device ->
//            list.add(device)
//        }
//        return list
//    }

    private fun adapterOnClick(device: WifiP2pDevice) {
        when (device.status) {
            0 -> disconnectDevice(device)
            1 -> cancelConnectionRequest(device)
            2, 3 -> connectDevice(device)
        }
    }

    private fun cancelConnectionRequest(device: WifiP2pDevice) {
        wifiP2pManager.cancelConnect(channel, null)
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

    @SuppressLint("MissingPermission")
    private fun connectDevice(device: WifiP2pDevice) {
        if (device.status == WifiP2pDevice.CONNECTED) disconnectDevice(device)
        val config = WifiP2pConfig()
        if (config.deviceAddress != null) {
            config.deviceAddress = device.deviceAddress
            config.wps.setup = WpsInfo.PBC // responsible for connecting without key/pincode

            wifiP2pManager.connect(channel, config, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Timber.d("Connection invitation to ${config.deviceAddress}")
                }

                override fun onFailure(reason: Int) {
                    Timber.d("Connection Failed: ${States.getDiscoveryFailureReason(reason)}")
                }
            })
        } else {
            Timber.i("Could not connect because address not found")
        }
    }

    private fun disconnectDevice(device: WifiP2pDevice) = wifiP2pManager.removeGroup(channel, null)

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