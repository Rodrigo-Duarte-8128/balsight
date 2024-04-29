package com.pocket_sight

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId


fun convertDecimalToEuroString(number: Double): String {
    val string: String = if (number == number.toInt().toDouble()) {
        "\u20ac ${number.toInt()}"
    } else {
        "\u20ac $number"
    }
    return string
}

fun convertMonthIntToString(monthInt: Int): String {
    return when(monthInt) {
        1 -> "Jan"
        2 -> "Feb"
        3 -> "Mar"
        4 -> "Apr"
        5 -> "May"
        6 -> "Jun"
        7 -> "Jul"
        8 -> "Aug"
        9 -> "Sep"
        10 -> "Oct"
        11 -> "Nov"
        else -> "Dec"
    }
}

fun parseMonthYearArrayToText(monthYearArray: Array<Int>): String {
    val monthInt = monthYearArray[0]
    val yearInt = monthYearArray[1]

    val monthString = convertMonthIntToString(monthInt)
    return "$monthString $yearInt"
}

fun parseMonthYearText(monthYearString: String): Array<Int> {
    val monthString = monthYearString.slice(1..3)
    val yearString = monthYearString.slice(5..<monthYearString.length)

    val monthInt = when (monthString) {
        "Jan" -> 1
        "Feb" -> 2
        "Mar" -> 3
        "Apr" -> 4
        "May" -> 5
        "Jun" -> 6
        "Jul" -> 7
        "Aug" -> 8
        "Sep" -> 9
        "Oct" -> 10
        "Nov" -> 11
        else -> 12
    }

    val yearInt = yearString.toInt()

    return arrayOf(monthInt, yearInt)
}



fun dateAfter(date: LocalDate, startDate: LocalDate): Boolean {
    if (date.year < startDate.year) {
        return false
    }

    if (date.year > startDate.year) {
        return true
    }
    // we are in the same year now
    if (date.monthValue < startDate.monthValue) {
        return false
    }

    if (date.monthValue > startDate.monthValue) {
        return true
    }
    // we are in the same month now
    if (date.dayOfMonth < startDate.dayOfMonth) {
        return false
    }
    return true
}


fun convertDateAndIdToDateTime(date: LocalDate, id: Int): LocalDateTime {
    // id must be between 0 and 86400
    val seconds: Int = id % 60
    val totalMinutes: Int = (id - seconds) / 60
    val minutes = totalMinutes % 60
    val hours = (totalMinutes - minutes) / 60
    val time = LocalTime.of(hours, minutes, seconds)
    return LocalDateTime.of(date, time)
}

fun convertDateAndIdToTimeMillis(date: LocalDate, id: Int): Long {
    // id must be between 0 and 86400
    val seconds: Int = id % 60
    val totalMinutes: Int = (id - seconds) / 60
    val minutes = totalMinutes % 60
    val hours = (totalMinutes - minutes) / 60
    val time = LocalTime.of(hours, minutes, seconds)
    val dateTime = LocalDateTime.of(date, time)
    return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}