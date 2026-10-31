package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.EmergencyContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyContactDao {
    @Query("SELECT * FROM emergency_contacts ORDER BY id ASC")
    fun getAllContacts(): Flow<List<EmergencyContactEntity>>

    @Query("SELECT * FROM emergency_contacts ORDER BY id ASC")
    suspend fun getAllContactsList(): List<EmergencyContactEntity>

    @Query("SELECT COUNT(*) FROM emergency_contacts")
    fun getContactCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContactEntity): Long

    @Update
    suspend fun updateContact(contact: EmergencyContactEntity)

    @Delete
    suspend fun deleteContact(contact: EmergencyContactEntity)
}
