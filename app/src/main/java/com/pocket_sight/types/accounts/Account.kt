package com.pocket_sight.types.accounts

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts_table")
data class Account(
    @PrimaryKey
    @ColumnInfo(name = "account_number")
    val number: Int,

    @ColumnInfo(name = "account_name")
    var name: String,

    @ColumnInfo(name="account_balance")
    var balance: Double,

    @ColumnInfo(name="main_account")
    var mainAccount: Boolean = false
)



