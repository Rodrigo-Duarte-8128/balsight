package com.pocket_sight.types.categories

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ProvisionalSubcategoriesDao{
    @Insert
    fun insert(subcategory: ProvisionalSubcategory)

    @Delete
    fun delete(subcategory: ProvisionalSubcategory)

    @Query("SELECT * from provisional_subcategories_table WHERE number = :key")
    fun get(key: Int): ProvisionalSubcategory

    @Query("SELECT number from provisional_subcategories_table ORDER BY number DESC LIMIT 1")
    fun getMaxNumber(): Int

    @Query("SELECT * from provisional_subcategories_table ORDER BY number")
    fun getAllProvisionalSubcategories(): List<ProvisionalSubcategory>

    @Query("SELECT * from provisional_subcategories_table WHERE parent_category_number = :parentKey")
    fun getProvisionalSubcategoriesWithParent(parentKey: Int): List<ProvisionalSubcategory>

    @Query("DELETE from provisional_subcategories_table")
    fun clearProvisionalSubcategories()

    @Query("SELECT COUNT(1) from provisional_subcategories_table WHERE provisional_subcategory_name = :name")
    fun nameInDatabase(name: String): Boolean
}

