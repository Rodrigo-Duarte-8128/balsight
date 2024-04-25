package com.pocket_sight.types.categories

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SubcategoriesDao {
    @Insert
    fun insert(subcategory: Subcategory)

    @Delete
    fun delete(subcategory: Subcategory)

    @Query("SELECT * from subcategories_table WHERE number = :key")
    fun get(key: Int): Subcategory

    @Query("SELECT number from subcategories_table ORDER BY number DESC LIMIT 1")
    fun getMaxNumber(): Int

    @Query("SELECT * from subcategories_table ORDER BY number")
    fun getAllSubcategories(): MutableList<Subcategory>

    @Query("SELECT * from subcategories_table WHERE parent_category_number = :parentKey")
    fun getSubcategoriesWithParent(parentKey: Int): List<Subcategory>

    @Query("DELETE from subcategories_table WHERE parent_category_number = :parentNumber")
    fun clearSubcategoriesWithParent(parentNumber: Int)

    @Query("DELETE FROM subcategories_table")
    fun clear()
}

