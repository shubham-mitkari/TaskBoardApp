# TaskBoardApp
A simple TaskBoard Android application built using Kotlin, Jetpack Compose, Room Database, and Hilt for dependency injection. The app allows users to create, edit, delete, and mark tasks as completed, with a simulated network sync feature.

## Features Walkthrough

### Task List Screen
- Displays all tasks in a scrollable list
- Shows task title, description, and completion status
- Checkbox to mark tasks complete/incomplete
- Delete button to remove tasks
- FAB (Floating Action Button) to add new tasks
- Sync button in toolbar to fetch dummy network data
- Empty state when no tasks exist
- Loading indicator during operations

### Add/Edit Task Screen
- Text fields for title and description
- Title validation (required field)
- Save button to create/update task
- Back navigation
- Form state management

### Sync Functionality
- Toolbar sync button
- Simulates network delay (1.5 seconds)
- Fetches 3 dummy tasks from "server"
- Merges/replaces local data
- Shows success/error feedback via Snackbar
- Loading indicator during sync

## Features

- Create, read, update, and delete tasks
- Mark tasks as complete/incomplete
- Sync tasks with dummy network source
- Real-time UI updates with Flow
- Material Design 3 UI
- Form validation and error handling
- Empty and loading states

## Architecture

This project follows **MVVM (Model-View-ViewModel)** with **Clean Architecture** principles:

```
TaskBoardApp/
├── app/
│   ├── MainActivity.kt                    # Navigation setup
│   └── TaskBoardApplication.kt            # Hilt application class
│
├── core/
│   ├── core-common/
│   │   └── Resource.kt                    # Result wrapper (Success/Error/Loading)
│   │
│   ├── core-data/
│   │   ├── local/
│   │   │   ├── TaskEntity.kt              # Room entity
│   │   │   ├── TaskDao.kt                 # Database operations
│   │   │   └── TaskDatabase.kt            # Room database configuration
│   │   ├── repository/
│   │   │   └── TaskRepository.kt          # Single source of truth
│   │   └── di/
│   │       └── DatabaseModule.kt          # Hilt database injection
│   │
│   └── core-network/
│       ├── DummyNetworkService.kt         # Simulated API calls
│       └── NetworkTask.kt                 # Network model
│
└── feature/
    └── feature-taskboard/
        ├── TaskViewModel.kt               # Business logic & state management
        ├── TaskListScreen.kt              # Main UI screen
        ├── AddEditTaskScreen.kt           # Add/Edit UI screen
        └── TaskUiState.kt                 # Sealed UI states
```

## Dependencies

```toml
[versions]
kotlin = "2.0.21"
agp = "8.9.1"
compose-bom = "2024.12.01"
hilt = "2.52"
room = "2.6.1"
coroutines = "1.9.0"
navigation-compose = "2.8.5"

[libraries]
# Jetpack Compose
androidx-compose-bom
androidx-ui
androidx-material3
androidx-navigation-compose

# Room Database
androidx-room-runtime
androidx-room-ktx
androidx-room-compiler (kapt)

# Hilt Dependency Injection
hilt-android
hilt-android-compiler (kapt)
androidx-hilt-navigation-compose

# Coroutines & Flow
kotlinx-coroutines-android
kotlinx-coroutines-core

# Lifecycle
androidx-lifecycle-runtime-ktx
androidx-lifecycle-viewmodel-compose
androidx-lifecycle-runtime-compose
```

## Module Structure

### app
Main application module:
- `MainActivity`: Entry point with Compose navigation setup
- `TaskBoardApplication`: Hilt application class annotated with @HiltAndroidApp
- Theme configuration

### core-common
Shared utilities:
- `Resource.kt`: Sealed class for handling operation states (Success, Error, Loading)

### core-data
Data layer:
- **local/**: Room database implementation
  - `TaskEntity`: Database table with @Entity annotation
  - `TaskDao`: DAO interface with @Query, @Insert, @Update, @Delete
  - `TaskDatabase`: RoomDatabase abstract class
- **repository/**: Data coordination
  - `TaskRepository`: Combines local and network data sources
- **di/**: Dependency injection
  - `DatabaseModule`: Provides Room database and DAO instances

### core-network
Network layer:
- `DummyNetworkService`: Simulates API with suspend functions and delay()
- `NetworkTask`: Data class for network response models

### feature-taskboard
UI layer:
- `TaskViewModel`: @HiltViewModel managing UI state with StateFlow
- `TaskListScreen`: Composable displaying task list with LazyColumn
- `AddEditTaskScreen`: Composable for task creation/editing forms
- `TaskUiState`: Sealed class representing UI states (Loading, Empty, Success, Error)
- `SyncState`: Sealed class for sync operation states

## Data Flow

1. User interacts with UI (Compose screens)
2. UI calls ViewModel functions
3. ViewModel invokes Repository methods
4. Repository accesses Room Database or Network Service
5. Room emits Flow when data changes
6. ViewModel collects Flow and updates StateFlow
7. UI observes StateFlow and recomposes automatically

## Key Implementation Details

### Room Database Entity
```kotlin
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val syncedWithNetwork: Boolean = false
)
```

### Repository Pattern
```kotlin
@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val networkService: DummyNetworkService
) {
    // Reactive data stream
    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()
    
    // CRUD operations
    suspend fun addTask(title: String, description: String): Resource<Long>
    suspend fun updateTask(task: TaskEntity): Resource<Unit>
    suspend fun deleteTask(task: TaskEntity): Resource<Unit>
    
    // Network sync
    suspend fun syncTasks(): Resource<Unit> {
        val networkTasks = networkService.fetchTasks()
        val entities = networkTasks.map { /* convert to TaskEntity */ }
        taskDao.insertTasks(entities)
        return Resource.Success(Unit)
    }
}
```

### Sealed UI State Management
```kotlin
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
```

### ViewModel State Management
```kotlin
@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<TaskUiState>(TaskUiState.Loading)
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()
    
    init {
        loadTasks()
    }
    
    private fun loadTasks() {
        viewModelScope.launch {
            repository.getAllTasks()
                .catch { exception ->
                    _uiState.value = TaskUiState.Error(exception.message ?: "Unknown error")
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
}
```

## Dependency Injection (Hilt)

### Application Setup
```kotlin
@HiltAndroidApp
class TaskBoardApplication : Application()
```

### Database Module
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideTaskDatabase(@ApplicationContext context: Context): TaskDatabase {
        return Room.databaseBuilder(
            context,
            TaskDatabase::class.java,
            "task_database"
        ).build()
    }
    
    @Provides
    @Singleton
    fun provideTaskDao(database: TaskDatabase): TaskDao {
        return database.taskDao()
    }
}
```

### Injected Components
- **TaskDatabase**: Singleton Room database instance
- **TaskDao**: Provided from database
- **TaskRepository**: Singleton with injected DAO and NetworkService
- **TaskViewModel**: Scoped to Activity with @HiltViewModel annotation
- **DummyNetworkService**: Singleton injected service

### ViewModel Injection in Compose
```kotlin
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel = hiltViewModel() // Hilt automatically injects
) {
    val uiState by viewModel.uiState.collectAsState()
    // UI implementation
}
```

## Error Handling

- **Network errors**: Wrapped in Resource.Error and shown via Snackbar
- **Database errors**: Caught in try-catch blocks and converted to Resource.Error
- **Input validation**: Real-time feedback with TextField isError parameter
- **Empty states**: Custom EmptyState composable with user-friendly messages
- **Loading states**: CircularProgressIndicator during async operations

---

**Note**: This project demonstrates modern Android development practices including Jetpack Compose, Clean Architecture, MVVM pattern, Room Database, Hilt dependency injection, and Kotlin Coroutines/Flow for reactive programming.
