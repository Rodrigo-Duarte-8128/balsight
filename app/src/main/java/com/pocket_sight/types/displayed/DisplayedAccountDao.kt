package com.pocket_sight.types.displayed

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query


@Dao
interface DisplayedAccountDao {
    @Insert
    fun insert(displayedAccount: DisplayedAccount)

    @Delete
    fun delete(displayedAccount: DisplayedAccount)

    @Query("SELECT * from displayed_account_table ORDER BY displayedAccountId")
    fun getAllDisplayedAccount(): List<DisplayedAccount>

    @Query("delete from displayed_account_table")
    fun clear()
}
