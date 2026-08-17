package com.brendan.controlanything.data.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class NsdDiscoveryRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : NsdDiscoveryRepository {

    override fun discoverBrokers(): Flow<DiscoveredBroker> = callbackFlow {
        val nsdManager = context.getSystemService(NsdManager::class.java)
        val wifiManager = context.applicationContext.getSystemService(WifiManager::class.java)
        // NSD relies on multicast; some OEM Wi-Fi stacks drop mDNS packets without this.
        val multicastLock = wifiManager?.createMulticastLock("controlanything-mdns")?.apply {
            setReferenceCounted(true)
        }
        multicastLock?.acquire()

        val discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(serviceType: String) = Unit

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                nsdManager.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) = Unit

                    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                        val host = serviceInfo.host?.hostAddress ?: return
                        trySend(DiscoveredBroker(serviceInfo.serviceName, host, serviceInfo.port))
                    }
                })
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) = Unit

            override fun onDiscoveryStopped(serviceType: String) = Unit

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                nsdManager.stopServiceDiscovery(this)
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) = Unit
        }

        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)

        awaitClose {
            multicastLock?.release()
            nsdManager.stopServiceDiscovery(discoveryListener)
        }
    }

    private companion object {
        const val SERVICE_TYPE = "_mqtt._tcp."
    }
}
