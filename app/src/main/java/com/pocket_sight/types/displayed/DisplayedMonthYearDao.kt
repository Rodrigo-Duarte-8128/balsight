package com.pocket_sight.types.displayed


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query


@Dao
interface DisplayedMonthYearDao {
    @Insert
    fun insert(displayedMonthYear: DisplayedMonthYear)

    @Delete
    fun delete(displayedMonthYear: DisplayedMonthYear)

    @Query("SELECT * from displayed_month_year_table ORDER BY monthYearId")
    fun getAllDisplayedMonthYear(): List<DisplayedMonthYear>
}
