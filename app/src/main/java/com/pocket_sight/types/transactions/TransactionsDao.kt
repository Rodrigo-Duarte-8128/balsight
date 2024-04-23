package com.pocket_sight.types.transactions

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TransactionsDao {
    @Insert
    fun insert(transaction: Transaction)

    @Delete
    fun delete(transaction: Transaction)

    @Query("SELECT * from transactions_table WHERE transactionId = :key")
    fun get(key: Int): Transaction

    @Query("SELECT * from transactions_table WHERE transaction_month_int = :month and transaction_year_int = :year ORDER BY transactionId DESC")
    fun getTransactionsFromMonthYear(month: Int, year: Int): List<Transaction>
}

