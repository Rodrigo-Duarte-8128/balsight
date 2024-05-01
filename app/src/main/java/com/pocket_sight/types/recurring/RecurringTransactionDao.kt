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

    @Query("update recurring_transactions_table set recurring_transaction_subcategory_number = null, recurring_transaction_old_subcategory_name = :oldSubName where recurring_transaction_subcategory_number = :subcategoryNumber")
    fun updateTransactionsWithRemovedSubcategory(subcategoryNumber: Int, oldSubName: String)

    @Query("update recurring_transactions_table set recurring_transaction_last_instantiation_day = :day, recurring_transaction_last_instantiation_month_int = :month, recurring_transaction_last_instantiation_year = :year where recurringTransactionId = :recurringTransactionId")
    fun updateInstantiationDate(recurringTransactionId: Int, day: Int, month: Int, year: Int)

    @Query("update recurring_transactions_table set recurring_transaction_account_number = :newAccountNumber where recurring_transaction_account_number = :oldAccountNumber")
    fun updateAccountNumber(oldAccountNumber: Int, newAccountNumber: Int)

    @Query("SELECT * from recurring_transactions_table ORDER BY recurringTransactionId")
    fun getAllRecurringTransactions(): List<RecurringTransaction>

    @Query("select * from recurring_transactions_table where recurring_transaction_account_number = :accountNumber order by recurringTransactionId")
    fun getAllRecurringTransactionsFromAccount(accountNumber: Int): List<RecurringTransaction>

    @Query("update recurring_transactions_table set recurring_transaction_category_number = :categoryNumber, recurring_transaction_subcategory_number = :subcategoryNumber, recurring_transaction_old_subcategory_name = :oldSubcategoryName, recurring_transaction_old_category_name = :oldCategoryName where recurringTransactionId = :recurringTransactionId")
    fun updateRecurringTransactionCats(
        recurringTransactionId: Int,
        categoryNumber: Int?,
        subcategoryNumber: Int?,
        oldSubcategoryName: String?,
        oldCategoryName: String?
    )

    @Query("select * from recurring_transactions_table where recurring_transaction_category_number = :categoryNumber order by recurringTransactionId desc")
    fun getRecurringTransactionsFromCategory(categoryNumber: Int): List<RecurringTransaction>

    @Query("select recurringTransactionId from recurring_transactions_table order by recurringTransactionId")
    fun getAllIds(): List<Int>

    @Query("delete from recurring_transactions_table where recurring_transaction_account_number = :accountNumber")
    fun deleteRecurringTransactionFromAccount(accountNumber: Int)

    @Query("delete from recurring_transactions_table")
    fun clear()
}
