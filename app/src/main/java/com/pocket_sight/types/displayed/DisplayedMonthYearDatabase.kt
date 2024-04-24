package com.pocket_sight.types.displayed


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DisplayedMonthYear::class], version = 1, exportSchema = false)
abstract class DisplayedMonthYearDatabase: RoomDatabase() {
    abstract val monthYearDao:  DisplayedMonthYearDao

    companion object {
        @Volatile
        private var INSTANCE: DisplayedMonthYearDatabase? = null

        fun getInstance(context: Context): DisplayedMonthYearDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        DisplayedMonthYearDatabase::class.java,
                        "displayed_month_year_database"
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}

