package com.pocket_sight.types.transactions

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pocket_sight.types.Act

@Entity(tableName = "transactions_table")
data class Transaction(
    @PrimaryKey
    var transactionId: Long, // this is the currentTimeMillis at the instant the transaction is created

    @ColumnInfo(name = "transaction_minutes")
    var minutes: Int = getMinutes(transactionId),

    @ColumnInfo(name = "transaction_hour")
    var hour: Int = getHour(transactionId),

    @ColumnInfo(name = "transaction_day")
    var day: Int = getDay(transactionId),

    @ColumnInfo(name = "transaction_month_int")
    var month: Int = getMonthInt(transactionId),

    @ColumnInfo(name = "transaction_year_int")
    var year: Int = getYear(transactionId),

    @ColumnInfo(name = "transaction_value")
    var value: Double,

    @ColumnInfo(name = "transaction_account_number")
    var accountNumber: Int,

    @ColumnInfo(name = "transaction_category_number")
    var categoryNumber: Int?,

    @ColumnInfo(name = "transaction_subcategory_number")
    var subcategory: Int?,

    @ColumnInfo(name = "transaction_note")
    var note: String,

    @ColumnInfo(name = "transaction_old_subcategory_name")
    var oldSubcategoryName: String?,

    @ColumnInfo(name = "transaction_old_category_name")
    var oldCategoryName: String?
): Act()


fun convertTimeMillisToLocalDateTime(timeMillis: Long): LocalDateTime {
    val instant: Instant = Instant.ofEpochMilli(timeMillis)
    val zone: ZoneId = ZoneId.systemDefault()
    val dateTime: ZonedDateTime = instant.atZone(zone)
    return dateTime.toLocalDateTime()
}

fun getMonthInt(timeMillis: Long): Int {
    val date = convertTimeMillisToLocalDateTime(timeMillis)
    return date.monthValue
}

fun getHour(timeMillis: Long): Int {
    val date = convertTimeMillisToLocalDateTime(timeMillis)
    return date.hour
}

fun getMinutes(timeMillis: Long): Int {
    val date = convertTimeMillisToLocalDateTime(timeMillis)
    return date.minute
}

fun getDay(timeMillis: Long): Int {
    val date = convertTimeMillisToLocalDateTime(timeMillis)
    return date.dayOfMonth
}

fun getYear(timeMillis: Long): Int {
    val date = convertTimeMillisToLocalDateTime(timeMillis)
    return date.year
}


