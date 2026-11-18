package com.example.trackrdata.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.trackrdata.data.dao.LocationDao
import com.example.trackrdata.data.dao.LocationSampleDao
import com.example.trackrdata.data.dao.VisitDao
import com.example.trackrdata.data.entities.Location
import com.example.trackrdata.data.entities.LocationSample
import com.example.trackrdata.data.entities.Visit

@Database(entities = [Location::class, Visit::class, LocationSample::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
    abstract fun visitDao(): VisitDao
    abstract fun locationSampleDao(): LocationSampleDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .createFromAsset("databases/demo.db")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}