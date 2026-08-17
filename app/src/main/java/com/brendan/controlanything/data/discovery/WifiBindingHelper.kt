package com.brendan.controlanything.data.discovery

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * The ESP32's AP has no internet uplink, so without explicit binding Android may route this
 * app's traffic over cellular (if active) instead, or never settle on the Wi-Fi network at all.
 * The user joins that network manually (via system Wi-Fi settings); this just observes whichever
 * Wi-Fi network is already connected and binds the app's own process traffic to it.
 *
 * Uses registerNetworkCallback rather than requestNetwork - the latter asks the system to
 * actively establish/prioritize a matching network, which for a bare transport-type request (no
 * NetworkSpecifier) requires CHANGE_NETWORK_STATE. Passive observation of an already-connected
 * network needs no special permission beyond ACCESS_NETWORK_STATE.
 */
class WifiBindingHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun bindToWifiNetwork(): Flow<Network> = callbackFlow {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                connectivityManager.bindProcessToNetwork(network)
                trySend(network)
            }

            override fun onLost(network: Network) {
                connectivityManager.bindProcessToNetwork(null)
            }
        }

        connectivityManager.registerNetworkCallback(request, callback)

        awaitClose {
            connectivityManager.bindProcessToNetwork(null)
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}
