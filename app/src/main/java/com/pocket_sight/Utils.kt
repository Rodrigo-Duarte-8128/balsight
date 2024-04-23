package com.pocket_sight


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

