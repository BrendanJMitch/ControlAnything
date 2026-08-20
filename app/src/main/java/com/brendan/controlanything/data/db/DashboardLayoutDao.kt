package com.brendan.controlanything.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface DashboardLayoutDao {
    @Query("SELECT * FROM placed_widgets WHERE projectId = :projectId")
    suspend fun getWidgets(projectId: String): List<PlacedWidgetEntity>

    @Upsert
    suspend fun upsertWidget(widget: PlacedWidgetEntity)

    @Upsert
    suspend fun upsertWidgets(widgets: List<PlacedWidgetEntity>)

    @Query("SELECT * FROM dashboard_settings WHERE projectId = :projectId")
    suspend fun getSettings(projectId: String): DashboardSettingsEntity?

    @Upsert
    suspend fun upsertSettings(settings: DashboardSettingsEntity)
}
