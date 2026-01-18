package com.example.examprep.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,

    @SerializedName("date")
    val date: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("ingredients")
    val ingredients: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("rating")
    val rating: Double
) : Parcelable
