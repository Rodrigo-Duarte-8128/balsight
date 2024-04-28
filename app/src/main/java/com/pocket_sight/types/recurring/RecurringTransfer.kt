package com.pocket_sight.types.recurring

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_transfers_table")
data class RecurringTransfer(
    @PrimaryKey
    var recurringTransferId: Int, // this is an integer which uniquely identifies this recurring transfer

    @ColumnInfo(name = "recurring_transfer_name")
    var name: String,

    @ColumnInfo(name = "recurring_transfer_month_day")
    var monthDay: Int,

    @ColumnInfo(name = "recurring_transfer_value")
    var value: Double,

    @ColumnInfo(name = "recurring_transfer_note")
    var note: String,

    @ColumnInfo(name = "recurring_transfer_account_sending_number")
    var accountSendingNumber: Int?,

    @ColumnInfo(name = "recurring_transfer_account_receiving_number")
    var accountReceivingNumber: Int?,

    @ColumnInfo(name = "recurring_transfer_start_day")
    var day: Int,

    @ColumnInfo(name = "recurring_transfer_start_month_int")
    var month: Int,

    @ColumnInfo(name = "recurring_transfer_start_year_int")
    var year: Int,

    @ColumnInfo(name = "recurring_transfer_last_instantiation_day")
    var lastInstantiationDay: Int?,

    @ColumnInfo(name = "recurring_transfer_last_instantiation_month_int")
    var lastInstantiationMonthInt: Int?,

    @ColumnInfo(name = "recurring_transfer_last_instantiation_year")
    var lastInstantiationYear: Int?
): RecurringAct()


