package com.brendan.controlanything.data.db

import androidx.room.Entity

/** Dashboard-level (not per-widget) settings for a saved (projectId, schemaHash) layout. */
@Entity(tableName = "dashboard_settings", primaryKeys = ["projectId", "schemaHash"])
data class DashboardSettingsEntity(
    val projectId: String,
    val schemaHash: String,
    val columnCount: Int,
    // DashboardOrientation.name - kept as a plain string rather than an enum so a renamed/removed
    // enum value doesn't break Room's generated column type.
    val orientation: String,
)
