package com.brendan.controlanything.data.db

import androidx.room.Entity

/** Dashboard-level (not per-widget) settings for a saved layout, keyed by projectId alone. */
@Entity(tableName = "dashboard_settings", primaryKeys = ["projectId"])
data class DashboardSettingsEntity(
    val projectId: String,
    val columnCount: Int,
    // DashboardOrientation.name - kept as a plain string rather than an enum so a renamed/removed
    // enum value doesn't break Room's generated column type.
    val orientation: String,
)
