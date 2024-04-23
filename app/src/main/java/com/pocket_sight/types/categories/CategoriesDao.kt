package com.pocket_sight.types.categories

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CategoriesDao {
    @Insert
    fun insert(category: Category)

    @Delete
    fun delete(category: Category)

    @Query("SELECT * from categories_table WHERE number = :key")
    fun get(key: Int): Category

    @Query("SELECT number from categories_table ORDER BY number DESC LIMIT 1")
    fun getMaxNumber(): Int

    @Query("SELECT * from categories_table ORDER BY number")
    fun getAllCategories(): MutableList<Category>

    @Query("SELECT COUNT(1) FROM categories_table WHERE category_name = :name")
    fun nameInDatabase(name: String): Boolean
}

