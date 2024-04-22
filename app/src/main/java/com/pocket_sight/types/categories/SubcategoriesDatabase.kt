package com.pocket_sight.types.categories

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Subcategory::class], version = 1, exportSchema = false)
abstract class SubcategoriesDatabase: RoomDatabase() {
    abstract val subcategoriesDatabaseDao: SubcategoriesDao

    companion object {
        @Volatile
        private var INSTANCE: SubcategoriesDatabase? = null

        fun getInstance(context: Context): SubcategoriesDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        SubcategoriesDatabase::class.java,
                        "subcategories_database"
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}

