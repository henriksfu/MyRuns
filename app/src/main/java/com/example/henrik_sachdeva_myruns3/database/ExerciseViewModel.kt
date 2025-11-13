package com.example.henrik_sachdeva_myruns3.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/**
 * ViewModel that provides data to the UI and handles user actions.
 * It interacts with the ExerciseRepository for all database operations.
 */
class ExerciseViewModel(private val repository: ExerciseRepository) : ViewModel() {

    // LiveData list of all exercise entries observed by the UI
    val allEntries: LiveData<List<ExerciseEntry>> = repository.allEntries

    /**
     * Insert a new exercise entry asynchronously.
     */
    fun insertEntry(exerciseEntry: ExerciseEntry) {
        viewModelScope.launch {
            repository.insertEntry(exerciseEntry)
        }
    }

    /**
     * Retrieve a specific entry by its ID.
     */
    fun getEntryById(entryId: Long): LiveData<ExerciseEntry> {
        return repository.getEntry(entryId)
    }

    /**
     * Delete an entry by ID asynchronously.
     */
    fun deleteEntry(entryId: Long) {
        viewModelScope.launch {
            repository.deleteEntry(entryId)
        }
    }
}

/**
 * Factory class used to create instances of ExerciseViewModel with a repository dependency.
 */
class ExerciseViewModelFactory(private val repository: ExerciseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExerciseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
