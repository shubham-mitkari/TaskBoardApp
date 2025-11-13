package com.example.taskboardapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.taskboardapp.feature.taskboard.com.example.feature_taskborad.AddEditTaskScreen
import com.example.taskboardapp.feature.taskboard.com.example.feature_taskborad.TaskListScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaskBoardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TaskBoardNavigation()
                }
            }
        }
    }
}

@Composable
fun TaskBoardNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "task_list"
    ) {
        // Task List Screen
        composable("task_list") {
            TaskListScreen(
                onNavigateToAddEdit = { taskId ->
                    if (taskId == null) {
                        navController.navigate("add_edit_task")
                    } else {
                        navController.navigate("add_edit_task?taskId=$taskId")
                    }
                }
            )
        }

        // Add/Edit Task Screen
        composable(
            route = "add_edit_task?taskId={taskId}",
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId")
            AddEditTaskScreen(
                taskId = if (taskId == -1) null else taskId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Alternative route without parameter
        composable("add_edit_task") {
            AddEditTaskScreen(
                taskId = null,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun TaskBoardTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}