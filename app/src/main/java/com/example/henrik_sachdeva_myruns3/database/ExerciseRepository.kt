package com.example.henrik_sachdeva_myruns3.database

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class ExerciseRepository(private val exerciseEntryDatabaseDao: ExerciseEntryDatabaseDao) {

    // Correct way to call DAO function
    val allEntries: LiveData<List<ExerciseEntry>> = exerciseEntryDatabaseDao.getAllEntries()

    suspend fun insertEntry(exerciseEntry: ExerciseEntry) {
        CoroutineScope(IO).launch {
            exerciseEntryDatabaseDao.insertEntry(exerciseEntry)
        }
    }

    fun getEntry(entryId: Long): LiveData<ExerciseEntry> {
        return exerciseEntryDatabaseDao.getEntryWithId(entryId)
    }

    fun deleteEntry(entryId: Long) {
        CoroutineScope(IO).launch {
            exerciseEntryDatabaseDao.deleteEntry(entryId)
        }
    }
}
