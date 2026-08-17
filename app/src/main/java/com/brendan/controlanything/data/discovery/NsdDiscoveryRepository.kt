package com.brendan.controlanything.data.discovery

import kotlinx.coroutines.flow.Flow

interface NsdDiscoveryRepository {
    fun discoverBrokers(): Flow<DiscoveredBroker>
}
