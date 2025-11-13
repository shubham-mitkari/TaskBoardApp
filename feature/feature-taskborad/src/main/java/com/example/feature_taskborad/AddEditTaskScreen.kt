package com.example.taskboardapp.feature.taskboard.com.example.feature_taskborad

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    taskId: Int?,
    viewModel: TaskViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    // Form state
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState, taskId) {
        if (taskId != null && uiState is TaskUiState.Success) {
            val task = (uiState as TaskUiState.Success).tasks.find { it.id == taskId }
            task?.let {
                title = it.title
                description = it.description
            }
        }
    }
    // Load existing task if editing
//    LaunchedEffect(taskId) {
//        taskId?.let { id ->
//            isLoading = true
//            // Get task from ViewModel's state
//            val uiState = viewModel.uiState.value
//            if (uiState is TaskUiState.Success) {
//                val task = uiState.tasks.find { it.id == id }
//                task?.let {
//                    title = it.title
//                    description = it.description
//                }
//            }
//            isLoading = false
//        }
//    }

    // Scope for coroutines
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (taskId == null) "New Task" else "Edit Task")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = title.isBlank()
                )

                // Description input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    maxLines = 10
                )

                Spacer(modifier = Modifier.weight(1f))

                // Save button
                Button(
                    onClick = {
                        if (title.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "Please enter a title"
                                )
                            }
                            return@Button
                        }

                        if (taskId == null) {
                            // Add new task
                            viewModel.addTask(title, description)
                        } else {
                            // Update existing task
                            val uiState = viewModel.uiState.value
                            if (uiState is TaskUiState.Success) {
                                val existingTask = uiState.tasks.find { it.id == taskId }
                                existingTask?.let { task ->
                                    viewModel.updateTask(
                                        task.copy(
                                            title = title,
                                            description = description
                                        )
                                    )
                                }
                            }
                        }

                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (taskId == null) "Add Task" else "Update Task")
                }
            }
        }
    }
}
