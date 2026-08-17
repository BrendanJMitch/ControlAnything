package com.brendan.controlanything.data.discovery

data class DiscoveredBroker(
    val serviceName: String,
    val host: String,
    val port: Int,
)
