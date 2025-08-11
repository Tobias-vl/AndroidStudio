package com.example.tictactoe

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pInfo
import android.os.Build
import androidx.core.app.ActivityCompat
import java.net.InetAddress


public class WiFiDirectBroadcastReceiver(
    private val manager: WifiP2pManager,
    private val channel: WifiP2pManager.Channel,
    private val activity: MainActivity
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                // Determine if Wi-Fi Direct mode is enabled or not, alert
                // the Activity.
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                activity.isWifiP2pEnabled = (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {

                    manager.requestPeers(channel) { peers ->
                        if (peers.deviceList.isNotEmpty()) {
                            Log.d("WiFiDirect", "Found peers: ${peers.deviceList}")
                            // Auto-connect to first peer (optional)
                            val firstPeer = peers.deviceList.first()
                            activity.connectToDevice(firstPeer)
                        } else {
                            Log.d("WiFiDirect", "No peers found")
                        }
                    }
                }
            }

            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                val networkInfo = intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
                if (networkInfo != null && networkInfo.isConnected) {
                    // Request connection info to find the group owner
                    manager.requestConnectionInfo(channel) { info: WifiP2pInfo ->
                        if (info.groupFormed && info.isGroupOwner) {
                            Log.d("WiFiDirect", "I am the host")
                            activity.startServer()
                        } else if (info.groupFormed) {
                            Log.d("WiFiDirect", "I am the client")
                            activity.startClient(info.groupOwnerAddress)
                        }
                    }
                } else {
                    Log.d("WiFiDirect", "Disconnected from Wi-Fi Direct peer")
                }
            }


            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                val device: WifiP2pDevice? =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE, WifiP2pDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)
                    }

                device?.let {
                    (activity.supportFragmentManager.findFragmentById(R.id.frag_list) as? DeviceListFragment)
                        ?.updateThisDevice(it)
                }
            }
            }
        }
    }

