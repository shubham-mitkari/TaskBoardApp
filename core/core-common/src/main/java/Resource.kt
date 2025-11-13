package com.example.taskboardapp.core.common


sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    // Success state - operation completed successfully with data
    class Success<T>(data: T) : Resource<T>(data)

    // Error state - operation failed with error message
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)

    // Loading state - operation is in progress
    class Loading<T>(data: T? = null) : Resource<T>(data)
}