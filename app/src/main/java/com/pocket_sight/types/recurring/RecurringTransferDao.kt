package com.pocket_sight.types.recurring

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query


@Dao
interface RecurringTransferDao{
    @Insert
    fun insert(recurringTransfer: RecurringTransfer)

    @Delete
    fun delete(recurringTransfer: RecurringTransfer)

    @Query("SELECT * from recurring_transfers_table WHERE recurringTransferId = :key")
    fun get(key: Int): RecurringTransfer

    @Query("update recurring_transfers_table set recurring_transfer_last_instantiation_day = :day, recurring_transfer_last_instantiation_month_int = :month, recurring_transfer_last_instantiation_year = :year where recurringTransferId = :recurringTransferId")
    fun updateInstantiationDate(recurringTransferId: Int, day: Int, month: Int, year: Int)

    @Query("update recurring_transfers_table set recurring_transfer_account_sending_number = :newAccountNumber where recurring_transfer_account_sending_number = :oldAccountNumber")
    fun updateAccountSending(oldAccountNumber: Int, newAccountNumber: Int)

    @Query("update recurring_transfers_table set recurring_transfer_account_receiving_number = :newAccountNumber where recurring_transfer_account_receiving_number = :oldAccountNumber")
    fun updateAccountReceiving(oldAccountNumber: Int, newAccountNumber: Int)

    @Query("SELECT * from recurring_transfers_table ORDER BY recurringTransferId")
    fun getAllRecurringTransfers(): List<RecurringTransfer>

    @Query("select recurringTransferId from recurring_transfers_table order by recurringTransferId")
    fun getAllIds(): List<Int>

    @Query("update recurring_transfers_table set recurring_transfer_account_sending_number = null where recurring_transfer_account_sending_number = :accountSendingNumber")
    fun setToNullAccountSending(accountSendingNumber: Int)

    @Query("update recurring_transfers_table set recurring_transfer_account_receiving_number = null where recurring_transfer_account_receiving_number = :accountReceivingNumber")
    fun setToNullAccountReceiving(accountReceivingNumber: Int)

    @Query("delete from recurring_transfers_table where recurring_transfer_account_receiving_number = null and recurring_transfer_account_sending_number = null")
    fun clearNullAccountTransfers()

    @Query("delete from recurring_transfers_table")
    fun clear()
}
