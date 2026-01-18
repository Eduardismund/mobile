package com.example.examprep.database

import androidx.room.*
import com.example.examprep.model.Course

@Dao
interface CourseDao {

    @Query("SELECT * FROM courses ORDER BY id DESC")
    suspend fun getAllCourses(): List<Course>

    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getCourseById(id: Int): Course?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(courses: List<Course>)

    @Update
    suspend fun updateCourse(course: Course)

    @Delete
    suspend fun deleteCourse(course: Course)

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM courses")
    suspend fun deleteAll()
}
