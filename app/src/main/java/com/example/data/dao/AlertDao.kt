package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts ORDER BY timestamp DESC LIMIT 1")
    fun getLatestAlert(): Flow<AlertEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity): Long

    @Query("DELETE FROM alerts")
    suspend fun clearAllAlerts()
}
