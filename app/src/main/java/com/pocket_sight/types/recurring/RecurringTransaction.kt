package com.pocket_sight.types.recurring

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_transactions_table")
data class RecurringTransaction(
    @PrimaryKey
    var recurringTransactionId: Int, // this is an integer which uniquely identifies this recurring transaction

    @ColumnInfo(name = "recurring_transaction_name")
    var name: String,

    @ColumnInfo(name = "recurring_transaction_month_day")
    var monthDay: Int,

    @ColumnInfo(name = "recurring_transaction_value")
    var value: Double,

    @ColumnInfo(name = "recurring_transaction_note")
    var note: String,

    @ColumnInfo(name = "recurring_transaction_category_number")
    var categoryNumber: Int?,

    @ColumnInfo(name = "recurring_transaction_subcategory_number")
    var subcategoryNumber: Int?,

    @ColumnInfo(name = "recurring_transaction_old_category_name")
    var oldCategoryName: String?,

    @ColumnInfo(name = "recurring_transaction_old_subcategory_name")
    var oldSubcategoryName: String?,

    @ColumnInfo(name = "recurring_transaction_account_number")
    var accountNumber: Int,

    @ColumnInfo(name = "recurring_transaction_start_day")
    var day: Int,

    @ColumnInfo(name = "recurring_transaction_start_month_int")
    var month: Int,

    @ColumnInfo(name = "recurring_transaction_start_year_int")
    var year: Int,

    @ColumnInfo(name = "recurring_transaction_last_instantiation_day")
    var lastInstantiationDay: Int?,

    @ColumnInfo(name = "recurring_transaction_last_instantiation_month_int")
    var lastInstantiationMonthInt: Int?,

    @ColumnInfo(name = "recurring_transaction_last_instantiation_year")
    var lastInstantiationYear: Int?
): RecurringAct()


