package com.example.examprep.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.examprep.database.AppDatabase
import com.example.examprep.model.Course
import com.example.examprep.repository.CourseRepository
import kotlinx.coroutines.launch

class CourseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CourseRepository

    init {
        val courseDao = AppDatabase.getDatabase(application).courseDao()
        repository = CourseRepository(courseDao)
    }

    private val _courses = MutableLiveData<List<Course>>()
    val courses: LiveData<List<Course>> = _courses

    private val _course = MutableLiveData<Course?>()
    val course: LiveData<Course?> = _course

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _operationSuccess = MutableLiveData<Boolean>()
    val operationSuccess: LiveData<Boolean> = _operationSuccess

    private val _allCoursesForAnalysis = MutableLiveData<List<Course>>()
    val allCoursesForAnalysis: LiveData<List<Course>> = _allCoursesForAnalysis

    fun loadCourses() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val coursesList = repository.getAllCourses()
                _courses.value = coursesList
                if (coursesList.isEmpty()) {
                    _errorMessage.value = "No courses found. Pull to refresh or check connection."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading courses: ${e.message}"
                // Try to load cached courses
                val cached = repository.getCachedCourses()
                _courses.value = cached
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadCoursesFromCache() {
        viewModelScope.launch {
            // Load from local database only (no network call)
            val cached = repository.getCachedCourses()
            _courses.value = cached
        }
    }

    fun getCourseById(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val courseData = repository.getCourseById(id)
                _course.value = courseData
            } catch (e: Exception) {
                _errorMessage.value = "Error loading course: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createCourse(course: Course) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.createCourse(course)
                if (response.isSuccessful) {
                    _operationSuccess.value = true
                    // No need to call loadCourses() - WebSocket will notify MainActivity
                    // and trigger the update automatically
                } else {
                    _errorMessage.value = "Failed to create course: ${response.message()}"
                    _operationSuccess.value = false
                }
            } catch (e: Exception) {
                // Provide more explicit error messages based on exception type
                val errorMsg = when {
                    e is java.net.ConnectException ||
                    e.message?.contains("Failed to connect", ignoreCase = true) == true ||
                    e.message?.contains("Connection refused", ignoreCase = true) == true ||
                    e.message?.contains("Connection reset", ignoreCase = true) == true ->
                        "Server is offline. Course saved locally and will sync when server is available."
                    e.message?.contains("timeout", ignoreCase = true) == true ->
                        "Server timeout. Course saved locally and will sync later."
                    e is java.net.UnknownHostException ->
                        "Network error. Course saved locally and will sync when online."
                    else ->
                        "Course saved locally: ${e.message ?: "Will sync when connection is available."}"
                }

                // Save to local database for offline support
                repository.cacheCourse(course)
                _errorMessage.value = errorMsg
                _operationSuccess.value = true // Still consider it success for offline mode
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCourse(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.deleteCourse(id)
                if (response.isSuccessful) {
                    // Remove from local list without server fetch
                    val currentList = _courses.value ?: emptyList()
                    _courses.value = currentList.filter { it.id != id }
                    _operationSuccess.value = true
                } else if (response.code() == 404) {
                    // Course not found on server - already deleted or never existed
                    // Delete from local cache and update UI
                    repository.deleteCourseFromCache(id)
                    val currentList = _courses.value ?: emptyList()
                    _courses.value = currentList.filter { it.id != id }
                    _operationSuccess.value = true
                } else {
                    _errorMessage.value = "Failed to delete course: ${response.message()}"
                    _operationSuccess.value = false
                }
            } catch (e: Exception) {
                // Provide more explicit error messages based on exception type
                val errorMsg = when {
                    e is java.net.ConnectException ||
                    e.message?.contains("Failed to connect", ignoreCase = true) == true ||
                    e.message?.contains("Connection refused", ignoreCase = true) == true ||
                    e.message?.contains("Connection reset", ignoreCase = true) == true ->
                        "Server is offline. Please make sure the server is running to delete courses."
                    e.message?.contains("timeout", ignoreCase = true) == true ->
                        "Server timeout. The server took too long to respond."
                    e is java.net.UnknownHostException ->
                        "Network error. Cannot reach the server."
                    else ->
                        "Error deleting course: ${e.message ?: "Unknown error occurred."}"
                }
                _errorMessage.value = errorMsg
                _operationSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAllCoursesForAnalysis() {
        viewModelScope.launch {
            try {
                // ONLINE ONLY - Always fetch from network using /allCourses endpoint
                // No cache fallback for Student and Analytics sections
                val fetchedCourses = repository.getAllCoursesComplete()
                _allCoursesForAnalysis.value = fetchedCourses
            } catch (e: Exception) {
                _errorMessage.value = "Error loading courses: ${e.message}"
                _allCoursesForAnalysis.value = emptyList() // Return empty list on error (no cache)
            }
        }
    }

    fun addCourseToList(course: Course) {
        // Add to UI list immediately (on main thread)
        val currentList = _courses.value ?: emptyList()
        _courses.value = listOf(course) + currentList

        // Cache to database for offline access (background thread)
        viewModelScope.launch {
            repository.cacheCourse(course)
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun resetOperationSuccess() {
        _operationSuccess.value = false
    }
}
