package com.example.tictactoe

import android.net.wifi.p2p.WifiP2pDevice
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment


class DeviceListFragment : Fragment() {

    private val devices = mutableListOf<WifiP2pDevice>()

    // Inflate the fragment layout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_device_list, container, false)
    }

    // Call this to update the device list UI
    fun updateDeviceList(newDevices: Collection<WifiP2pDevice>) {
        devices.clear()
        devices.addAll(newDevices)
        // TODO: Update UI to show devices (e.g., notify RecyclerView adapter)
    }

    // Update info about this device
    fun updateThisDevice(device: WifiP2pDevice) {
        // TODO: Update UI elements showing your own device info
    }
}
