package com.pocket_sight.types.recurring

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RecurringTransfer::class], version = 1, exportSchema = false)
abstract class RecurringTransferDatabase: RoomDatabase() {
    abstract val recurringTransferDao: RecurringTransferDao

    companion object {
        @Volatile
        private var INSTANCE: RecurringTransferDatabase? = null

        fun getInstance(context: Context): RecurringTransferDatabase{
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        RecurringTransferDatabase::class.java,
                        "recurring_transfer_database"
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}

