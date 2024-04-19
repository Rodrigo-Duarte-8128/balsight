package com.pocket_sight.types

data class Account(
    val number: Int,
    var name: String,
    var balance: Double,
    var current: Boolean = false
)



