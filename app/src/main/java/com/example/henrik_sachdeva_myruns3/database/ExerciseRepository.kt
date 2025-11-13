package com.example.henrik_sachdeva_myruns3.database

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

/**
 * Repository layer that abstracts data operations.
 * It mediates between the DAO and the ViewModel.
 */
class ExerciseRepository(private val exerciseEntryDatabaseDao: ExerciseEntryDatabaseDao) {

    // Observed LiveData notifies observers whenever the database updates
    val allEntries: LiveData<List<ExerciseEntry>> = exerciseEntryDatabaseDao.getAllEntries()

    /**
     * Insert a new exercise entry asynchronously using a background thread.
     */
    suspend fun insertEntry(exerciseEntry: ExerciseEntry) {
        CoroutineScope(IO).launch {
            exerciseEntryDatabaseDao.insertEntry(exerciseEntry)
        }
    }

    /**
     * Fetch a single entry by its ID.
     */
    fun getEntry(entryId: Long): LiveData<ExerciseEntry> {
        return exerciseEntryDatabaseDao.getEntryWithId(entryId)
    }

    /**
     * Delete a specific entry by ID using coroutines on the IO dispatcher.
     */
    fun deleteEntry(entryId: Long) {
        CoroutineScope(IO).launch {
            exerciseEntryDatabaseDao.deleteEntry(entryId)
        }
    }
}
