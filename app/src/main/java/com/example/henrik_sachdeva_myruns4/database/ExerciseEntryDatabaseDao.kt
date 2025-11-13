package com.example.henrik_sachdeva_myruns4.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExerciseEntryDatabaseDao {

    @Insert
    fun insertEntry(entry: ExerciseEntry)

    @Query("SELECT * FROM exercise_table ORDER BY id DESC")
    fun getAllEntries(): LiveData<List<ExerciseEntry>>

    @Query("SELECT * FROM exercise_table WHERE id = :entryId LIMIT 1")
    fun getEntryWithId(entryId: Long): LiveData<ExerciseEntry>

    @Query("SELECT * FROM exercise_table WHERE id = :entryId LIMIT 1")
    fun getEntryNow(entryId: Long): ExerciseEntry

    @Query("DELETE FROM exercise_table WHERE id = :entryId")
    fun deleteEntry(entryId: Long)
}
