package com.brendan.controlanything.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface DashboardLayoutDao {
    @Query("SELECT * FROM placed_widgets WHERE projectId = :projectId AND schemaHash = :schemaHash")
    suspend fun getWidgets(projectId: String, schemaHash: String): List<PlacedWidgetEntity>

    @Upsert
    suspend fun upsertWidget(widget: PlacedWidgetEntity)

    @Upsert
    suspend fun upsertWidgets(widgets: List<PlacedWidgetEntity>)

    @Query("SELECT * FROM dashboard_settings WHERE projectId = :projectId AND schemaHash = :schemaHash")
    suspend fun getSettings(projectId: String, schemaHash: String): DashboardSettingsEntity?

    @Upsert
    suspend fun upsertSettings(settings: DashboardSettingsEntity)
}
