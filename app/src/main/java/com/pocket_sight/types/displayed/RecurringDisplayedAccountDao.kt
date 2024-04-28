package com.pocket_sight.types.displayed

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query


@Dao
interface RecurringDisplayedAccountDao{
    @Insert
    fun insert(recurringDisplayedAccount: RecurringDisplayedAccount)

    @Delete
    fun delete(recurringDisplayedAccount: RecurringDisplayedAccount)

    @Query("SELECT * from recurring_displayed_account_table ORDER BY recurringDisplayedAccountId")
    fun getAllRecurringDisplayedAccount(): List<RecurringDisplayedAccount>

    @Query("delete from recurring_displayed_account_table")
    fun clear()
}
