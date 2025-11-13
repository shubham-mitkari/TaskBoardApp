package com.example.taskboardapp.feature.taskboard.com.example.feature_taskborad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core_data.TaskEntity
import com.example.core_data.TaskRepository
import com.example.taskboardapp.core.common.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    // UI State - Represents what the UI should show
    private val _uiState = MutableStateFlow<TaskUiState>(TaskUiState.Loading)
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    // Sync state - For showing sync progress
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    init {
        // Load tasks when ViewModel is created
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            repository.getAllTasks()
                .catch { exception ->
                    _uiState.value = TaskUiState.Error(
                        exception.message ?: "Unknown error"
                    )
                }
                .collect { tasks ->
                    _uiState.value = if (tasks.isEmpty()) {
                        TaskUiState.Empty
                    } else {
                        TaskUiState.Success(tasks)
                    }
                }
        }
    }


    // Add a new task
    fun addTask(title: String, description: String) {
        if (title.isBlank()) {
            return
        }

        viewModelScope.launch {
            repository.addTask(title, description)
        }
    }


    // Update existing task

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    //  Toggle task completion
    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task)
        }
    }

    // Delete a task
    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // Sync with network
    fun syncTasks() {
        viewModelScope.launch {
            _syncState.value = SyncState.Syncing

            when (val result = repository.syncTasks()) {
                is Resource.Success -> {
                    _syncState.value = SyncState.Success
                    // Reset to idle after 2 seconds
                    delay(2000)
                    _syncState.value = SyncState.Idle
                }

                is Resource.Error -> {
                    _syncState.value = SyncState.Error(
                        result.message ?: "Sync failed"
                    )
                }

                else -> {}
            }
        }
    }
}

sealed class TaskUiState {
    object Loading : TaskUiState()
    object Empty : TaskUiState()
    data class Success(val tasks: List<TaskEntity>) : TaskUiState()
    data class Error(val message: String) : TaskUiState()
}

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    object Success : SyncState()
    data class Error(val message: String) : SyncState()
}