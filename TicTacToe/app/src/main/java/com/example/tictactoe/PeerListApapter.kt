package com.example.tictactoe

import android.net.wifi.p2p.WifiP2pDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PeerListAdapter(
    private val peers: List<WifiP2pDevice>,
    private val onItemClick: (WifiP2pDevice) -> Unit
) : RecyclerView.Adapter<PeerListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = peers[position]
        holder.name.text = device.deviceName ?: device.deviceAddress
        holder.itemView.setOnClickListener { onItemClick(device) }
    }

    override fun getItemCount(): Int = peers.size

    fun updateThisDevice(it: WifiP2pDevice) {
        peers.toMutableList().add(0, it)
        notifyItemInserted(0)
    }
}


