package com.pocket_sight.types.recurring

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RecurringTransaction::class], version = 1, exportSchema = false)
abstract class RecurringTransactionsDatabase: RoomDatabase() {
    abstract val recurringTransactionsDao: RecurringTransactionsDao

    companion object {
        @Volatile
        private var INSTANCE: RecurringTransactionsDatabase? = null

        fun getInstance(context: Context): RecurringTransactionsDatabase{
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        RecurringTransactionsDatabase::class.java,
                        "recurring_transactions_database"
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}

