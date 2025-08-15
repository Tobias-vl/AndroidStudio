package com.example.tictactoe

import android.net.wifi.p2p.WifiP2pDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PeerListAdapter(
    private var peers: List<WifiP2pDevice>,
    private val onClick: (WifiP2pDevice) -> Unit
) : RecyclerView.Adapter<PeerListAdapter.PeerViewHolder>() {

    inner class PeerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById(R.id.deviceName)
        val deviceStatus: TextView = itemView.findViewById(R.id.deviceStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_device_list, parent, false)
        return PeerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PeerViewHolder, position: Int) {
        val device = peers[position]
        holder.deviceName.text = device.deviceName
        holder.deviceStatus.text = getDeviceStatus(device.status)

        holder.itemView.setOnClickListener { onClick(device) }
    }

    override fun getItemCount() = peers.size

    fun updateData(newPeers: List<WifiP2pDevice>) {
        peers = newPeers
        notifyDataSetChanged()
    }

    private fun getDeviceStatus(status: Int): String {
        return when (status) {
            WifiP2pDevice.AVAILABLE -> "Available"
            WifiP2pDevice.INVITED -> "Invited"
            WifiP2pDevice.CONNECTED -> "Connected"
            WifiP2pDevice.FAILED -> "Failed"
            WifiP2pDevice.UNAVAILABLE -> "Unavailable"
            else -> "Unknown"
        }
    }
}

 fun PeerListAdapter?.updateThisDevice(it: WifiP2pDevice) {
            TODO("Not yet implemented")
}
