package com.example.examprep.repository

import android.util.Log
import com.example.examprep.database.CourseDao
import com.example.examprep.model.Course
import com.example.examprep.network.RetrofitClient
import retrofit2.Response

class CourseRepository(private val courseDao: CourseDao) {

    private val apiService = RetrofitClient.apiService
    private val TAG = "CourseRepository"

    suspend fun getAllCourses(forceRefresh: Boolean = false): List<Course> {
        return try {
            Log.d(TAG, "Fetching courses from network")
            val response = apiService.getAllCourses()
            if (response.isSuccessful) {
                val courses = response.body() ?: emptyList()
                // Cache to database
                courseDao.insertAll(courses)
                Log.d(TAG, "Cached ${courses.size} courses to database")
                courses
            } else {
                Log.e(TAG, "Network error: ${response.code()}")
                // Return cached data on error
                getCachedCourses()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching courses: ${e.message}")
            // Return cached data on exception
            getCachedCourses()
        }
    }

    suspend fun getCachedCourses(): List<Course> {
        Log.d(TAG, "Fetching courses from local database")
        return courseDao.getAllCourses()
    }

    suspend fun getCourseById(id: Int): Course? {
        return try {
            Log.d(TAG, "Fetching course $id from network")
            val response = apiService.getCourseById(id)
            if (response.isSuccessful) {
                val course = response.body()
                course?.let {
                    courseDao.insertCourse(it)
                    Log.d(TAG, "Cached course $id to database")
                }
                course
            } else {
                Log.e(TAG, "Network error: ${response.code()}")
                courseDao.getCourseById(id)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching course: ${e.message}")
            courseDao.getCourseById(id)
        }
    }

    suspend fun createCourse(course: Course): Response<Course> {
        Log.d(TAG, "Creating course: ${course.name}")
        return apiService.createCourse(course)
    }

    suspend fun deleteCourse(id: Int): Response<Unit> {
        Log.d(TAG, "Deleting course: $id")
        val response = apiService.deleteCourse(id)
        if (response.isSuccessful) {
            courseDao.deleteById(id)
            Log.d(TAG, "Deleted course $id from local database")
        }
        return response
    }

    suspend fun deleteCourseFromCache(id: Int) {
        Log.d(TAG, "Deleting course $id from local cache only (not found on server)")
        courseDao.deleteById(id)
    }

    suspend fun cacheCourse(course: Course) {
        Log.d(TAG, "Caching course ${course.id} to local database")
        courseDao.insertCourse(course)
    }

    suspend fun getAllCoursesComplete(): List<Course> {
        // Online-only - no cache fallback for Student and Analytics sections
        Log.d(TAG, "Fetching all courses (complete) from /allCourses endpoint - ONLINE ONLY")
        val response = apiService.getAllCoursesComplete()
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            Log.e(TAG, "Network error: ${response.code()}")
            throw Exception("Failed to fetch courses: ${response.message()}")
        }
    }
}
