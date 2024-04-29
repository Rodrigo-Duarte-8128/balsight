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
    @Query("SELECT * from recurring_transfers_table ORDER BY recurringTransferId")
    fun getAllRecurringTransfers(): List<RecurringTransfer>

    @Query("select recurringTransferId from recurring_transfers_table order by recurringTransferId")
    fun getAllIds(): List<Int>

    @Query("delete from recurring_transfers_table")
    fun clear()
}
