package com.example.henrik_sachdeva_myruns4.database

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class ExerciseRepository(
    private val dao: ExerciseEntryDatabaseDao
) {

    val allEntries: LiveData<List<ExerciseEntry>> = dao.getAllEntries()

    suspend fun insertEntry(entry: ExerciseEntry) {
        CoroutineScope(IO).launch {
            dao.insertEntry(entry)
        }
    }

    fun getEntry(entryId: Long): LiveData<ExerciseEntry> {
        return dao.getEntryWithId(entryId)
    }

    fun deleteEntry(entryId: Long) {
        CoroutineScope(IO).launch {
            dao.deleteEntry(entryId)
        }
    }
}
