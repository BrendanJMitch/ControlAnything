package com.brendan.controlanything.di

import com.brendan.controlanything.data.discovery.NsdDiscoveryRepository
import com.brendan.controlanything.data.discovery.NsdDiscoveryRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DiscoveryModule {
    @Binds
    abstract fun bindNsdDiscoveryRepository(impl: NsdDiscoveryRepositoryImpl): NsdDiscoveryRepository
}
