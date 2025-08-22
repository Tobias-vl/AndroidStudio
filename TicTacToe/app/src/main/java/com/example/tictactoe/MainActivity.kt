package com.example.tictactoe

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
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
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.tictactoe.databinding.ActivityMainBinding
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

public final class MainActivity : AppCompatActivity() {
    var isWifiP2pEnabled: Boolean = false

    private val intentFilter = IntentFilter()
    private var deviceListFragment: DeviceListFragment? = null

    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var manager: WifiP2pManager
    private lateinit var receiver: WiFiDirectBroadcastReceiver

    private lateinit var peerListListener: WifiP2pManager.PeerListListener
    private lateinit var connectionListener: WifiP2pManager.ConnectionInfoListener
    private lateinit var hostbutton: Button

    private val peers = mutableListOf<WifiP2pDevice>()

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        connectionListener = createConnectionList(false)

        receiver = WiFiDirectBroadcastReceiver(
            manager,
            channel,
            this,
            peerListListener,
            connectionListener
        )

        // Host button
        binding.hostButton.setOnClickListener {
            Toast.makeText(this, "Hosting game...", Toast.LENGTH_SHORT).show()
            // Force create group and wait for client
            connectionListener = createConnectionList(true)
            // When group is created, your BroadcastReceiver will call connectionListener
        }

        // Join button
        binding.joinButton.setOnClickListener {
            deviceListFragment = DeviceListFragment { device ->
                // when user clicks a peer -> connect to it
                connectToPeer(device)
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, deviceListFragment!!)
                .addToBackStack(null)
                .commit()

            discoverPeers()
        }
    }

    fun connectToPeer(device : WifiP2pDevice) {
        if (peers.isEmpty()) {
            Toast.makeText(this, "No peers available", Toast.LENGTH_SHORT).show()
            return
        }
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
                Toast.makeText(this@MainActivity, "Connecting to ${device.deviceName}", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(reason: Int) {
                Toast.makeText(this@MainActivity, "Connect failed. Retry.", Toast.LENGTH_SHORT).show()
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

    fun createPeersList(): WifiP2pManager.PeerListListener {
        return WifiP2pManager.PeerListListener { peerList ->
            val refreshedPeers = peerList.deviceList
            peers.clear()
            peers.addAll(refreshedPeers)

            // update fragment UI
            deviceListFragment?.updatePeers(refreshedPeers)

            if (peers.isEmpty()) {
                Log.d(TAG, "No devices found")
            }
        }
    }

    fun createConnectionList(boolean: Boolean): WifiP2pManager.ConnectionInfoListener {
        connectionListener = WifiP2pManager.ConnectionInfoListener { info ->
            val groupOwnerAddress: String = info.groupOwnerAddress.hostAddress
            if (boolean) {
                Toast.makeText(this, "This device is the group owner", Toast.LENGTH_SHORT).show()
                thread {
                    try {
                        val serverSocket = ServerSocket(8080)
                        val client = serverSocket.accept()
                        val input = client.getInputStream()
                        val output = client.getOutputStream()

                        val intent = Intent(
                            this,
                            HostJoin::class.java
                        )  // HostJoin is the GameScreen activity
                        // Optionally pass extras: e.g. intent.putExtra("isHost", info.isGroupOwner)
                        startActivity(intent)
                    }
                    catch (e: IOException) {
                        e.printStackTrace()
                    }
                }

            } else {
                Toast.makeText(this, "This device is the client, group owner: $groupOwnerAddress", Toast.LENGTH_SHORT).show()
                thread {
                    try {
                        val socket = Socket()
                        socket.connect(InetSocketAddress(groupOwnerAddress, 8080), 5000)
                        val input = socket.getInputStream()
                        val output = socket.getOutputStream()

                        val intent = Intent(this, HostJoin::class.java)  // HostJoin is the GameScreen activity
                        // Optionally pass extras: e.g. intent.putExtra("isHost", info.isGroupOwner)
                        startActivity(intent)
                    }
                    catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return connectionListener
    }
}


