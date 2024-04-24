package com.pocket_sight.types.displayed

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DisplayedAccount::class], version = 1, exportSchema = false)
abstract class DisplayedAccountDatabase : RoomDatabase() {
    abstract val displayedAccountDao: DisplayedAccountDao

    companion object {
        @Volatile
        private var INSTANCE: DisplayedAccountDatabase? = null

        fun getInstance(context: Context): DisplayedAccountDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        DisplayedAccountDatabase::class.java,
                        "displayed_account_database"
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}

