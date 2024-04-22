package com.pocket_sight.types.categories

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "subcategories_table")
data class Subcategory(
    @PrimaryKey
    val number: Int,

    @ColumnInfo(name = "subcategory_name")
    var name: String,

    @ColumnInfo(name="parent_category_number")
    var parentNumber: Int
)





