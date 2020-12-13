package com.example.myapplication.adapter

import android.net.wifi.p2p.WifiP2pDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.util.States

class WifiP2pPeersAdapter(private val onClick: (WifiP2pDevice) -> Unit) :
    ListAdapter<WifiP2pDevice, WifiP2pPeersAdapter.PeerDeviceVH>(WifiP2pDeviceDiffCallback) {

    class PeerDeviceVH(itemView: View, val onClick: (WifiP2pDevice) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val tvDeviceName: TextView = itemView.findViewById(R.id.device_name)
        private val tvConnectionStatus: TextView = itemView.findViewById(R.id.connection_status)
        private var currentWifiP2pDevice: WifiP2pDevice? = null

        init {
            itemView.setOnClickListener {
                currentWifiP2pDevice?.let {
                    onClick(it)
                }
            }
        }

        /* Bind device name and it's status of connection */
        fun bind(device: WifiP2pDevice) {
            tvDeviceName.text = device.deviceName
            tvConnectionStatus.text = States.getDeviceStatus(device.status)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeerDeviceVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(getItemViewType(viewType), parent, false)
        return PeerDeviceVH(view, onClick)
    }

    override fun onBindViewHolder(holder: PeerDeviceVH, position: Int) {
        val device = getItem(position)
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_p2p_device
    }
}

object WifiP2pDeviceDiffCallback : DiffUtil.ItemCallback<WifiP2pDevice>() {
    override fun areItemsTheSame(oldItem: WifiP2pDevice, newItem: WifiP2pDevice): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: WifiP2pDevice, newItem: WifiP2pDevice): Boolean {
        return (oldItem.deviceName == newItem.deviceName) && (oldItem.status == newItem.status)
    }

}
