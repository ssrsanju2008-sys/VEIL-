package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AlertDao
import com.example.data.dao.EmergencyContactDao
import com.example.data.model.AlertEntity
import com.example.data.model.EmergencyContactEntity

@Database(
    entities = [AlertEntity::class, EmergencyContactEntity::class],
    version = 1,
    exportSchema = false
)
abstract class VeilDatabase : RoomDatabase() {
    abstract fun alertDao(): AlertDao
    abstract fun emergencyContactDao(): EmergencyContactDao

    companion object {
        @Volatile
        private var INSTANCE: VeilDatabase? = null

        fun getDatabase(context: Context): VeilDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VeilDatabase::class.java,
                    "veil_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
