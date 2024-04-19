package com.pocket_sight.types

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AccountsDao {
    @Insert
    fun insert(account: Account)

    @Delete
    fun delete(account: Account)

    @Query("SELECT * from accounts_table WHERE number = :key")
    fun get(key: Int): Account
}