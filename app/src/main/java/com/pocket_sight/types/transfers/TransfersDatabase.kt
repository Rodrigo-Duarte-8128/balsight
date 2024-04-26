package com.pocket_sight.types.transfers

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Transfer::class], version = 1, exportSchema = false)
abstract class TransfersDatabase: RoomDatabase() {
    abstract val transfersDao: TransfersDao

    companion object {
        @Volatile
        private var INSTANCE: TransfersDatabase? = null

        fun getInstance(context: Context): TransfersDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        TransfersDatabase::class.java,
                        "transfers_database"
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}

