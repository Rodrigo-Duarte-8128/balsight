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

    @Query("SELECT * from transactions_table WHERE transaction_month_int = :month and transaction_year_int = :year and transaction_account_number = :accountNumber ORDER BY transactionId DESC")
    fun getTransactionsFromMonthYear(month: Int, year: Int, accountNumber: Int): List<Transaction>

    //@Query("select * from transactions_table where transaction_account_number = :accountNumber order by transactionId desc")
    //fun getTransactionsFromAccount(accountNumber: Int): List<Transaction>

    @Query("update transactions_table set transaction_account_number = :newAccountNumber where transaction_account_number = :oldAccountNumber")
    fun updateAccountNumber(oldAccountNumber: Int, newAccountNumber: Int)

    @Query("update transactions_table set transaction_value = -transaction_value where transaction_category_number = :categoryNumber")
    fun changeValuesOfCategory(categoryNumber: Int)

    @Query("update transactions_table set transaction_subcategory_number = null, transaction_old_subcategory_name = :oldSubName where transaction_subcategory_number = :subcategoryNumber")
    fun updateTransactionsWithRemovedSubcategory(subcategoryNumber: Int, oldSubName: String)

    @Query("update transactions_table set transaction_value = :value, transaction_account_number = :accountNumber, transaction_category_number = :categoryNumber, transaction_subcategory_number = :subcategoryNumber, transaction_note = :note, transaction_old_subcategory_name = :oldSubcategoryName, transaction_old_category_name = :oldCategoryName where transactionId = :transactionId")
    fun updateTransaction(
        transactionId: Long,
        value: Double,
        accountNumber: Int,
        categoryNumber: Int?,
        subcategoryNumber: Int?,
        note: String,
        oldSubcategoryName: String?,
        oldCategoryName: String?
    )

    @Query("select * from transactions_table where transaction_category_number = :categoryNumber order by transactionId desc")
    fun getTransactionsFromCategory(categoryNumber: Int): List<Transaction>

    @Query("DELETE FROM transactions_table")
    fun clear()
}

