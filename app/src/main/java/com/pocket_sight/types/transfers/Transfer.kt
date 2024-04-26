package com.pocket_sight.types.transfers

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pocket_sight.types.Act

@Entity(tableName = "transfers_table")
data class Transfer(
    @PrimaryKey
    var transferId: Long, // this is the currentTimeMillis at the instant the transfer is created

    @ColumnInfo(name = "transfer_minute")
    var minute: Int,

    @ColumnInfo(name = "transfer_hour")
    var hour: Int,

    @ColumnInfo(name = "transfer_day")
    var day: Int,

    @ColumnInfo(name = "transfer_month_int")
    var month: Int,

    @ColumnInfo(name = "transfer_year")
    var year: Int,

    @ColumnInfo(name = "transfer_value")
    var value: Double,

    @ColumnInfo(name = "transfer_note")
    var note: String,

    @ColumnInfo(name = "transfer_account_sending_number")
    var accountSendingNumber: Int?,

    @ColumnInfo(name = "transfer_account_receiving_number")
    var accountReceivingNumber: Int?
): Act()
