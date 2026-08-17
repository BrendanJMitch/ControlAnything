package com.brendan.controlanything.di

import com.brendan.controlanything.data.mqtt.HiveMqRepository
import com.brendan.controlanything.data.mqtt.MqttRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MqttModule {
    @Binds
    @Singleton
    abstract fun bindMqttRepository(impl: HiveMqRepository): MqttRepository
}
