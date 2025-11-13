package com.example.henrik_sachdeva_myruns4

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.henrik_sachdeva_myruns4.database.ExerciseEntry
import com.example.henrik_sachdeva_myruns4.database.ExerciseRepository
import kotlinx.coroutines.launch

class ExerciseViewModel(
    private val repository: ExerciseRepository
) : ViewModel() {

    val allEntries: LiveData<List<ExerciseEntry>> = repository.allEntries

    fun insertEntry(entry: ExerciseEntry) {
        viewModelScope.launch {
            repository.insertEntry(entry)
        }
    }

    fun getEntryById(entryId: Long): LiveData<ExerciseEntry> {
        return repository.getEntry(entryId)
    }

    fun deleteEntry(entryId: Long) {
        viewModelScope.launch {
            repository.deleteEntry(entryId)
        }
    }
}

class ExerciseViewModelFactory(
    private val repository: ExerciseRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExerciseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
