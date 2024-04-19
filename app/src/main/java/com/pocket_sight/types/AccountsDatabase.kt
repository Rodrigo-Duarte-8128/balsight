package com.pocket_sight.types

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Account::class], version = 1, exportSchema = false)
abstract class AccountsDatabase: RoomDatabase() {
    abstract val accountsDao: AccountsDao

    companion object {
        @Volatile
        private var INSTANCE: AccountsDatabase? = null

        fun getInstance(context: Context): AccountsDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AccountsDatabase::class.java,
                        "accounts_database"
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}

