package com.pocket_sight.types.first_run_tracker

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query


@Dao
interface FirstRunTrackerDao{
    @Insert
    fun insert(firstRunTracker: FirstRunTracker)

    @Delete
    fun delete(firstRunTracker: FirstRunTracker)

    @Query("SELECT * from first_run_tracker_table ORDER BY `key`")
    fun getAllTrackers(): List<FirstRunTracker>

    @Query("delete from first_run_tracker_table")
    fun clear()
}
