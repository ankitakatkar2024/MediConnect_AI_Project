package com.example.mediconnect_ai

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SosLog::class], version = 1, exportSchema = false)
abstract class SosLogDatabase : RoomDatabase() {
    abstract fun sosLogDao(): SosLogDao

    companion object {
        @Volatile
        private var INSTANCE: SosLogDatabase? = null

        fun getDatabase(context: Context): SosLogDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    SosLogDatabase::class.java,
                    "sos_logs_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
