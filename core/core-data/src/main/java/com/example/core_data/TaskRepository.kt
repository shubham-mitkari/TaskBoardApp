package com.example.core_data

import com.example.core_network.DummyNetworkService
import com.example.taskboardapp.core.common.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val networkService: DummyNetworkService
) {


    fun getAllTasks(): Flow<List<TaskEntity>> {
        return taskDao.getAllTasks()
    }


    suspend fun getTaskById(id: Int): TaskEntity? {
        return taskDao.getTaskById(id)
    }

    suspend fun addTask(title: String, description: String): Resource<Long> {
        return try {
            val task = TaskEntity(
                title = title,
                description = description,
                isCompleted = false
            )
            val id = taskDao.insertTask(task)
            Resource.Success(id)
        } catch (e: Exception) {
            Resource.Error("Failed to add task: ${e.message}")
        }
    }


    suspend fun updateTask(task: TaskEntity): Resource<Unit> {
        return try {
            taskDao.updateTask(task)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to update task: ${e.message}")
        }
    }


    suspend fun toggleTaskCompletion(task: TaskEntity): Resource<Unit> {
        return try {
            val updatedTask = task.copy(isCompleted = !task.isCompleted)
            taskDao.updateTask(updatedTask)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to toggle task: ${e.message}")
        }
    }

    suspend fun deleteTask(task: TaskEntity): Resource<Unit> {
        return try {
            taskDao.deleteTask(task)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to delete task: ${e.message}")
        }
    }

    suspend fun syncTasks(): Resource<Unit> {
        return try {
            // Fetch from network
            val networkTasks = networkService.fetchTasks()

            // Convert network models to database entities
            val taskEntities = networkTasks.map { networkTask ->
                TaskEntity(
                    id = networkTask.id,
                    title = networkTask.title,
                    description = networkTask.description,
                    isCompleted = networkTask.isCompleted,
                    syncedWithNetwork = true
                )
            }

            // Save to database (will replace if ID exists)
            taskDao.insertTasks(taskEntities)

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Sync failed: ${e.message}")
        }
    }
}