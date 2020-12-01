package com.example.myapplication.adapter

import android.net.wifi.p2p.WifiP2pDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.util.States

class WifiP2pPeersAdapter(private var peerDevices: ArrayList<WifiP2pDevice?>? = arrayListOf()) :
    RecyclerView.Adapter<WifiP2pPeersAdapter.PeerDeviceVH>() {

    class PeerDeviceVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var connectionStatus: String? = null
        var tvDeviceName: TextView = itemView.findViewById(R.id.device_name)
        var tvConnectionStatus: TextView = itemView.findViewById(R.id.connection_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeerDeviceVH {
        val itemView =
            LayoutInflater.from(parent.context).inflate(getItemViewType(viewType), parent, false)
        return PeerDeviceVH(itemView)
    }

    override fun onBindViewHolder(holder: PeerDeviceVH, position: Int) {
        holder.connectionStatus = peerDevices?.get(position)?.deviceAddress
        holder.tvDeviceName.text = peerDevices?.get(position)?.deviceName
        holder.tvConnectionStatus.text = States.getDeviceStatus(peerDevices?.get(position)?.status)
    }

    override fun getItemCount(): Int {
        return peerDevices?.size ?: 0
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_p2p_device
    }
}