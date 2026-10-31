package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val source: String, // "Wristband button" or "Test alert"
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Float? = null,
    val address: String? = null,
    val isResolved: Boolean = true
)
