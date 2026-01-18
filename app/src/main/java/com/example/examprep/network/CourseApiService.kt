package com.example.examprep.network

import com.example.examprep.model.Course
import retrofit2.Response
import retrofit2.http.*

interface CourseApiService {

    @GET("courses")
    suspend fun getAllCourses(): Response<List<Course>>

    @GET("course/{id}")
    suspend fun getCourseById(@Path("id") id: Int): Response<Course>

    @POST("course")
    suspend fun createCourse(@Body course: Course): Response<Course>

    @DELETE("course/{id}")
    suspend fun deleteCourse(@Path("id") id: Int): Response<Unit>

    @GET("allCourses")
    suspend fun getAllCoursesComplete(): Response<List<Course>>
}
