package com.pocket_sight.types.transfers

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TransfersDao{
    @Insert
    fun insert(transfer: Transfer)

    @Delete
    fun delete(transfer: Transfer)

    @Query("delete from transfers_table where transferId = :timeMillis")
    fun deleteByKey(timeMillis: Long)

    @Query("SELECT * from transfers_table WHERE transferId = :key")
    fun get(key: Long): Transfer

    @Query("SELECT * from transfers_table WHERE transfer_month_int = :month and transfer_year = :year ORDER BY transferId DESC")
    fun getTransfersFromMonthYear(month: Int, year: Int): List<Transfer>

    @Query("select * from transfers_table where transfer_day = :day and transfer_month_int = :month and transfer_year = :year order by transferId")
    fun getTransfersFromDay(day: Int, month: Int, year: Int): List<Transfer>

    @Query("update transfers_table set transfer_value = :value, transfer_account_sending_number = :accountSendingNumber, transfer_account_receiving_number = :accountReceivingNumber, transfer_note = :note where transferId = :transferId")
    fun updateTransfer(
        transferId: Long,
        value: Double,
        accountSendingNumber: Int,
        accountReceivingNumber: Int,
        note: String,
    )

    @Query("select count(1) from transfers_table where transferId = :timeMillis")
    fun idInDatabase(timeMillis: Long): Boolean

    @Query("update transfers_table set transfer_account_sending_number = :newAccountNumber where transfer_account_sending_number = :oldAccountNumber")
    fun updateAccountSending(oldAccountNumber: Int, newAccountNumber: Int)

    @Query("update transfers_table set transfer_account_receiving_number = :newAccountNumber where transfer_account_receiving_number = :oldAccountNumber")
    fun updateAccountReceiving(oldAccountNumber: Int, newAccountNumber: Int)

    @Query("update transfers_table set transfer_account_sending_number = null where transfer_account_sending_number = :accountSendingNumber")
    fun setToNullAccountSending(accountSendingNumber: Int)

    @Query("update transfers_table set transfer_account_receiving_number = null where transfer_account_receiving_number = :accountReceivingNumber")
    fun setToNullAccountReceiving(accountReceivingNumber: Int)

    @Query("delete from transfers_table where transfer_account_receiving_number = null and transfer_account_sending_number = null")
    fun clearNullAccountTransfers()

    @Query("DELETE FROM transfers_table")
    fun clear()
}

