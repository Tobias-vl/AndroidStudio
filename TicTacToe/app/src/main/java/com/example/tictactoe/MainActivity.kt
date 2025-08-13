package com.example.tictactoe

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat

public final class MainActivity : AppCompatActivity() {
    var isWifiP2pEnabled: Boolean = false

    private val intentFilter = IntentFilter()
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var manager: WifiP2pManager
    private lateinit var receiver: WiFiDirectBroadcastReceiver

    private lateinit var peerListListener: WifiP2pManager.PeerListListener
    private lateinit var connectionListener: WifiP2pManager.ConnectionInfoListener

    private val peers = mutableListOf<WifiP2pDevice>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Indicates a change in the Wi-Fi Direct status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)

        // Indicates the state of Wi-Fi Direct connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)

        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)

        // Initialize listeners
        peerListListener = createPeersList()
        connectionListener = createConnectionList()

        receiver = WiFiDirectBroadcastReceiver(
            manager,
            channel,
            this,
            peerListListener,
            connectionListener
        )
    }

    fun connectToFirstPeer() {
        if (peers.isEmpty()) {
            Toast.makeText(this, "No peers available", Toast.LENGTH_SHORT).show()
            return
        }
        // Picking the first device found on the network.
        val device = peers[0]

        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
            wps.setup = WpsInfo.PBC
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
            return
        }

        manager.connect(channel, config, object : WifiP2pManager.ActionListener {

            override fun onSuccess() {
                Log.d(TAG, "Connecting to ${device.deviceName}")
            }

            override fun onFailure(reason: Int) {
                Toast.makeText(
                    this@MainActivity,
                    "Connect failed. Retry.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }


    fun discoverPeers() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
            return
        }
        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Peer discovery started")
            }

            override fun onFailure(reasonCode: Int) {
                Log.e(TAG, "Peer discovery failed: $reasonCode")
            }
        })
    }



    public override fun onResume() {
        super.onResume()
        receiver = WiFiDirectBroadcastReceiver(
            manager,
            channel,
            this,
            peerListListener,
            connectionListener
        )
        registerReceiver(receiver, intentFilter)
        discoverPeers()
    }

    public override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }

    fun requestPeers() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        peerListListener = WifiP2pManager.PeerListListener { peerList ->
            val refreshedPeers = peerList.deviceList
            if (refreshedPeers != peers) {
                peers.clear()
                peers.addAll(refreshedPeers)
            }

            if (peers.isEmpty()) {
                Log.d(TAG, "No devices found")
                return@PeerListListener
            }
        }
    }

    fun requestGroupInfo() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                102
            )
            return
        }
        manager.requestGroupInfo(channel) { group ->
            val groupPassword = group.passphrase
        }
    }

    fun createPeersList(): WifiP2pManager.PeerListListener {

        val peerListListener = WifiP2pManager.PeerListListener { peerList ->
            val refreshedPeers = peerList.deviceList
            if (refreshedPeers != peers) {
                peers.clear()
                peers.addAll(refreshedPeers)
            }

            if (peers.isEmpty()) {
                Log.d(TAG, "No devices found")
                return@PeerListListener
            }
        }
        return peerListListener
    }

    fun createConnectionList(): WifiP2pManager.ConnectionInfoListener {
        connectionListener = WifiP2pManager.ConnectionInfoListener { info ->
            val groupOwnerAddress: String = info.groupOwnerAddress.hostAddress
            if (info.groupFormed && info.isGroupOwner) {
                Log.d(TAG, "This device is the group owner")
                // Start server tasks here
            } else if (info.groupFormed) {
                Log.d(TAG, "This device is the client, group owner: $groupOwnerAddress")
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    manager.requestGroupInfo(channel) { group ->
                        val groupPassword = group.passphrase
                    }
                }
                // Connect to the group owner here
            }
        }
        return connectionListener
    }
}

