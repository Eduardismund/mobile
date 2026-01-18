package com.example.examprep.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "courses")
data class Course(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("instructor")
    val instructor: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("status")
    val status: String, // "upcoming", "ongoing", "completed"

    @SerializedName("students")
    val students: Int,

    @SerializedName("duration")
    val duration: Int // in hours
) : Parcelable
