package com.pocket_sight

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId


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


fun convertLocalDateTimeToMillis(date: LocalDateTime): Long {
    return date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

fun recurringActOccursThisMonthYear(month: Int, year: Int, startDay: Int, startMonth: Int, startYear: Int, recurringDay: Int): Boolean {
    if (year < startYear) {
        return false
    }

    if (year > startYear) {
        return true
    }

    // now year == startYear
    if (month < startMonth) {
        return false
    }

    if (month > startMonth) {
        return true
    }

    // now month == startMonth

    if (recurringDay < startDay) {
        return false
    }

    return true
}