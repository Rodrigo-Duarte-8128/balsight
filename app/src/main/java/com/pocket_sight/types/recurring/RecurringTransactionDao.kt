package com.pocket_sight.types.recurring

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.pocket_sight.types.transactions.Transaction


@Dao
interface RecurringTransactionsDao{
    @Insert
    fun insert(recurringTransaction: RecurringTransaction)

    @Delete
    fun delete(recurringTransaction: RecurringTransaction)

    @Query("SELECT * from recurring_transactions_table WHERE recurringTransactionId = :key")
    fun get(key: Int): RecurringTransaction

    @Query("SELECT * from recurring_transactions_table ORDER BY recurringTransactionId")
    fun getAllRecurringTransactions(): List<RecurringTransaction>

    @Query("select * from recurring_transactions_table where recurring_transaction_account_number = :accountNumber order by recurringTransactionId")
    fun getAllRecurringTransactionsFromAccount(accountNumber: Int): List<RecurringTransaction>

    @Query("select recurringTransactionId from recurring_transactions_table order by recurringTransactionId")
    fun getAllIds(): List<Int>

    @Query("delete from recurring_transactions_table")
    fun clear()
}
