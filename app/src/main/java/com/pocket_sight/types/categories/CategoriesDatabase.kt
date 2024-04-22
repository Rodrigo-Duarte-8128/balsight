package com.pocket_sight.types.categories

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [Category::class], version = 1, exportSchema = false)
abstract class CategoriesDatabase: RoomDatabase() {
    abstract val categoriesDatabaseDao: CategoriesDao

    companion object {
        @Volatile
        private var INSTANCE: CategoriesDatabase? = null

        fun getInstance(context: Context): CategoriesDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        CategoriesDatabase::class.java,
                        "categories_database"
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}

