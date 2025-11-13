package com.example.henrik_sachdeva_myruns3.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExerciseEntryDatabaseDao {

    @Insert
    fun insertEntry(exerciseEntry: ExerciseEntry)

    @Query("SELECT * FROM exercise_table")
    fun getAllEntries(): LiveData<List<ExerciseEntry>>

    @Query("SELECT * FROM exercise_table WHERE id = :entryId")
    fun getEntryWithId(entryId: Long): LiveData<ExerciseEntry>

    @Query("DELETE FROM exercise_table WHERE id = :entryId")
    fun deleteEntry(entryId: Long)
}
