package com.example.tictactoe

import android.net.wifi.p2p.WifiP2pDevice
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DeviceListFragment(
    private val onDeviceClick: (WifiP2pDevice) -> Unit
) : Fragment(R.layout.fragment_device_list) {

    private lateinit var adapter: PeerListAdapter
    private val peers = mutableListOf<WifiP2pDevice>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_device_list, container, false)
        val recyclerView = root.findViewById<RecyclerView>(R.id.peerRecyclerView)

        adapter = PeerListAdapter(peers) { device ->
            onDeviceClick(device)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        return root
    }

    fun updatePeers(newPeers: Collection<WifiP2pDevice>) {
        peers.clear()
        peers.addAll(newPeers)
        if (this::adapter.isInitialized) {
            adapter.notifyDataSetChanged()
        }
    }
}
