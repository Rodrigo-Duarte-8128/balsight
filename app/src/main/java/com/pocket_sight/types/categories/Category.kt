package com.pocket_sight.types.categories

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "categories_table")
data class Category(
    @PrimaryKey
    val number: Int,

    @ColumnInfo(name = "category_name")
    var name: String, // category names are UNIQUE!!!

    @ColumnInfo(name="category_kind")
    var kind: String // kind is "Income" or "Expense"
)






