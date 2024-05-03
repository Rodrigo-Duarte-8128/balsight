package com.pocket_sight.types.first_run_tracker

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FirstRunTracker::class], version = 1, exportSchema = false)
abstract class FirstRunTrackerDatabase: RoomDatabase() {
    abstract val firstRunTrackerDao: FirstRunTrackerDao

    companion object {
        @Volatile
        private var INSTANCE: FirstRunTrackerDatabase? = null

        fun getInstance(context: Context): FirstRunTrackerDatabase{
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        FirstRunTrackerDatabase::class.java,
                        "first_run_tracker_database"
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}

