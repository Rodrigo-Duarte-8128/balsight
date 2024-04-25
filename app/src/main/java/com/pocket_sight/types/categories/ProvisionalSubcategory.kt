package com.pocket_sight.types.categories

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "provisional_subcategories_table")
data class ProvisionalSubcategory(
    @PrimaryKey
    val number: Int,

    @ColumnInfo(name = "provisional_subcategory_name")
    var name: String,

    @ColumnInfo(name="parent_category_number")
    var parentNumber: Int,

    @ColumnInfo(name="associated_subcategory_number")
    var associatedSubcategoryNumber: Int?
)





