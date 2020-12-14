package com.example.myapplication.ui.viewmodel

import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.util.DirectActionListener
import timber.log.Timber

class FirstFragmentViewModel : ViewModel() {
    val onSelfDeviceAvailable: MutableLiveData<WifiP2pDevice> = MutableLiveData()
    val isWifiP2pEnabled: MutableLiveData<Boolean> = MutableLiveData()
    private val onConnectionInfoAvailable: MutableLiveData<WifiP2pInfo> = MutableLiveData()
    val onPeersAvailable: MutableLiveData<Collection<WifiP2pDevice>> = MutableLiveData()

    val directActionListener: Any =
        object : DirectActionListener, WifiP2pManager.ChannelListener {

            override fun wifiP2pEnabled(enabled: Boolean) {
                isWifiP2pEnabled.value = enabled
            }

            override fun onConnectionInfoAvailable(wifiP2pInfo: WifiP2pInfo) {
                onConnectionInfoAvailable.value = wifiP2pInfo
            }

            override fun onDisconnection() {
                Timber.i("Disconnected from group")
            }

            override fun onSelfDeviceAvailable(wifiP2pDevice: WifiP2pDevice) {
                onSelfDeviceAvailable.value = wifiP2pDevice
            }

            override fun onPeersAvailable(wifiP2pDeviceList: Collection<WifiP2pDevice>) {
                onPeersAvailable.value = wifiP2pDeviceList
                Timber.tag("onPeersAvailable").i(wifiP2pDeviceList.toString())
            }

            override fun onChannelDisconnected() {
                Timber.i("Channel Disconnected")
            }

        }

}

sealed class WifiP2pConnectionStatus{
    object NONE : WifiP2pConnectionStatus()
    object CONNECTED : WifiP2pConnectionStatus()
    object AVAILABLE : WifiP2pConnectionStatus()
    object INVITED : WifiP2pConnectionStatus()
}