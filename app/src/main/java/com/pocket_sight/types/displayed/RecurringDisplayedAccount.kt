package com.pocket_sight.types.displayed

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "recurring_displayed_account_table")
data class RecurringDisplayedAccount(
    @PrimaryKey
    var recurringDisplayedAccountId: Int, // always 1 when the table isn't empty

    @ColumnInfo(name = "recurring_displayed_account_number")
    var displayedAccountNumber: Int
)
