package com.example.wifimanager

import android.content.Context
import android.net.NetworkInfo
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.ArrayAdapter
import android.content.IntentFilter
import android.content.Intent
import android.content.BroadcastReceiver
import android.net.ConnectivityManager


class MainActivity : AppCompatActivity() {
    val wifiReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val networkInfo = cm.activeNetworkInfo

                if (networkInfo == null) {
                    Toast.makeText(context, "No Network available", Toast.LENGTH_SHORT).show()
                } else if (networkInfo != null && networkInfo.type == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected) {
                    val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                    val wifiInfo = wifiManager.connectionInfo
                    val ssid = wifiInfo.ssid
                    Toast.makeText(context, "Wifi connected, " + " SSID " + ssid, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        showSavedNetworks()

        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(wifiReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(wifiReceiver)
    }

    fun connect(view : View) {
        val wifiConfig = WifiConfiguration()
        wifiConfig.SSID = String.format("\"%s\"", et_wifi_ssid.text)
        wifiConfig.preSharedKey = String.format("\"%s\"", et_wifi_password.text)
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val netId = wifiManager.addNetwork(wifiConfig)
        wifiManager.enableNetwork(netId, true)

        showSavedNetworks()
    }

    fun addToNetworkList(view : View) {
        val wifiConfig = WifiConfiguration()
        wifiConfig.SSID = String.format("\"%s\"", et_wifi_ssid.text)
        wifiConfig.preSharedKey = String.format("\"%s\"", et_wifi_password.text)
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.addNetwork(wifiConfig)

        showSavedNetworks()
    }

    fun removeFromNetworkList(view : View) {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        for (network in wifiManager.configuredNetworks) {
            var ssid = network.SSID
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length - 1)
            }
            if (ssid == et_wifi_ssid.text.toString()) {
                wifiManager.removeNetwork(network.networkId)
            }
        }

        showSavedNetworks()
    }


    fun showCurrentlyConnectedWifiNetwork(view : View) {
        val manager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        var ssid = ""

        if (manager.isWifiEnabled) {
            val wifiInfo = manager.connectionInfo
            if (wifiInfo != null) {
                val state = WifiInfo.getDetailedStateOf(wifiInfo.supplicantState)
                if (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {

                    ssid = wifiInfo.ssid
                    if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                        ssid = ssid.substring(1, ssid.length - 1)
                    }
                }
            }
        }

        Toast.makeText(this, ssid, Toast.LENGTH_SHORT).show()
    }


    fun showSavedNetworks() {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val networks = ArrayList<String>()
        for (network in wifiManager.configuredNetworks) {
            networks.add(network.SSID)
        }

        val arrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, networks)
        list_view.setAdapter(arrayAdapter)
    }

}
