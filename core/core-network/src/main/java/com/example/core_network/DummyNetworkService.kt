package com.example.core_network

import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DummyNetworkService @Inject constructor() {

    suspend fun fetchTasks(): List<NetworkTask> {
        delay(1500)

        // Return fake data as if coming from API
        return listOf(
            NetworkTask(
                id = 1,
                title = "Update LinkedIn Profile",
                description = "Add recent projects and refresh profile summary",
                isCompleted = true
            ),
            NetworkTask(
                id = 2,
                title = "Apply for Android Developer Role",
                description = "Submit application to 3 companies",
                isCompleted = false
            ),
            NetworkTask(
                id = 3,
                title = "Prepare for Technical Interview",
                description = "Revise Kotlin, Coroutines, and Jetpack Compose basics",
                isCompleted = false
            )
        )
    }

    suspend fun syncTask(task: NetworkTask): Boolean {
        delay(500)
        return true
    }
}

data class NetworkTask(
    val id: Int,
    val title: String,
    val description: String,
    val isCompleted: Boolean
)