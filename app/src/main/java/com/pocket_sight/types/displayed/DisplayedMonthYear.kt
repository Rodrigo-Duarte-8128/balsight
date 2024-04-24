package com.pocket_sight.types.displayed

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "displayed_month_year_table")
data class DisplayedMonthYear (
    @PrimaryKey
    var monthYearId: Int,

    @ColumnInfo(name = "displayed_month_int")
    var month: Int,

    @ColumnInfo(name = "displayed_year_int")
    var year: Int
)