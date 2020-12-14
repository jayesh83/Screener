package com.example.myapplication.util

import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager

object States {
    public var isWifiP2pEnabled = false

    fun getDeviceStatus(deviceStatus: Int?): String {
        return when (deviceStatus) {
            WifiP2pDevice.AVAILABLE -> "Available"
            WifiP2pDevice.INVITED -> "Invited"
            WifiP2pDevice.CONNECTED -> "Connected"
            WifiP2pDevice.FAILED -> "Failed"
            WifiP2pDevice.UNAVAILABLE -> "Unavailable"
            else -> "Unknown"
        }
    }

    fun getDiscoveryFailureReason(reason: Int?): String {
        return when (reason) {
            WifiP2pManager.ERROR -> "Internal Error"
            WifiP2pManager.P2P_UNSUPPORTED -> "Wifi P2P Unsupported"
            WifiP2pManager.BUSY -> "Busy"
            else -> "Unknown"
        }
    }
}