package com.pocket_sight.types.first_run_tracker

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "first_run_tracker_table")
data class FirstRunTracker(
    @PrimaryKey
    var key: Int, // always 1 when the table isn't empty

    @ColumnInfo(name = "ran")
    var ran: Boolean
)
