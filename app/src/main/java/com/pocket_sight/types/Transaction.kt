package com.pocket_sight.types

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

data class Transaction (
    var transactionId: Long, // this is the currentTimeMillis at the instant the transaction is created
    var category: String,
    var value: Double,
    var date: LocalDateTime = convertTimeMillisToLocalDateTime(transactionId)
)


fun convertTimeMillisToLocalDateTime(timeMillis: Long): LocalDateTime {
    val instant: Instant = Instant.ofEpochMilli(timeMillis)
    val zone: ZoneId = ZoneId.of("Europe/Lisbon")
    val dateTime: ZonedDateTime = instant.atZone(zone)
    return dateTime.toLocalDateTime()
}


