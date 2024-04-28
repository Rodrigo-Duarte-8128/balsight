package com.pocket_sight.types.displayed

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RecurringDisplayedAccount::class], version = 1, exportSchema = false)
abstract class RecurringDisplayedAccountDatabase: RoomDatabase() {
    abstract val recurringDisplayedAccountDao: RecurringDisplayedAccountDao

    companion object {
        @Volatile
        private var INSTANCE: RecurringDisplayedAccountDatabase? = null

        fun getInstance(context: Context): RecurringDisplayedAccountDatabase{
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        RecurringDisplayedAccountDatabase::class.java,
                        "recurring_displayed_account_database"
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}

