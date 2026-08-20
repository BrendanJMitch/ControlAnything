package com.brendan.controlanything.di

import android.content.Context
import androidx.room.Room
import com.brendan.controlanything.data.db.ControlAnythingDatabase
import com.brendan.controlanything.data.db.DashboardLayoutDao
import com.brendan.controlanything.data.layout.LayoutRepository
import com.brendan.controlanything.data.layout.RoomLayoutRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {
    @Binds
    abstract fun bindLayoutRepository(impl: RoomLayoutRepository): LayoutRepository

    companion object {
        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): ControlAnythingDatabase =
            Room.databaseBuilder(context, ControlAnythingDatabase::class.java, "control_anything.db")
                // Saved layouts are a rearrangeable local cache, not irreplaceable data - fine to
                // drop and recreate on a schema bump rather than writing a real Migration.
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()

        @Provides
        fun provideDashboardLayoutDao(database: ControlAnythingDatabase): DashboardLayoutDao =
            database.dashboardLayoutDao()
    }
}
