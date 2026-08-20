package com.brendan.controlanything.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PlacedWidgetEntity::class, DashboardSettingsEntity::class],
    version = 2,
)
abstract class ControlAnythingDatabase : RoomDatabase() {
    abstract fun dashboardLayoutDao(): DashboardLayoutDao
}
