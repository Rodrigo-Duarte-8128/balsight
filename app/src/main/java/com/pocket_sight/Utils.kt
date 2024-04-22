package com.pocket_sight


fun convertDecimalToEuroString(number: Double): String {
    val string: String = if (number == number.toInt().toDouble()) {
        "\u20ac ${number.toInt()}"
    } else {
        "\u20ac $number"
    }
    return string
}

