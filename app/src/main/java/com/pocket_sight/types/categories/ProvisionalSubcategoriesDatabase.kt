package com.pocket_sight.types.categories

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ProvisionalSubcategory::class], version = 1, exportSchema = false)
abstract class ProvisionalSubcategoriesDatabase: RoomDatabase() {
    abstract val provisionalSubcategoriesDatabaseDao: ProvisionalSubcategoriesDao

    companion object {
        @Volatile
        private var INSTANCE: ProvisionalSubcategoriesDatabase? = null

        fun getInstance(context: Context): ProvisionalSubcategoriesDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ProvisionalSubcategoriesDatabase::class.java,
                        "provisional_subcategories_database"
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}

