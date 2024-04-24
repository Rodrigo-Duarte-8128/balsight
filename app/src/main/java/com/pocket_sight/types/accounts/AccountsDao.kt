package com.pocket_sight.types.accounts

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.pocket_sight.types.accounts.Account

@Dao
interface AccountsDao {
    @Insert
    fun insert(account: Account)

    @Delete
    fun delete(account: Account)

    @Query("SELECT * from accounts_table WHERE number = :key")
    fun get(key: Int): Account

    @Query("SELECT number from accounts_table ORDER BY number DESC LIMIT 1")
    fun getMaxNumber(): Int

    @Query("UPDATE accounts_table SET main_account = 0")
    fun setMainToFalse()

    @Query("SELECT * from accounts_table ORDER BY number")
    fun getAllAccounts(): MutableList<Account>

    @Query("SELECT COUNT(1) FROM accounts_table WHERE number = :number")
    fun accountNumberInDatabase(number: Int): Boolean

    @Query("SELECT account_number FROM accounts_table WHERE main_account = 1")
    fun getMainAccountNumber(): Int?
}

