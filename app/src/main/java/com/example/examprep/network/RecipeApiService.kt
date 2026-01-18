package com.example.examprep.network

import com.example.examprep.model.Recipe
import retrofit2.Response
import retrofit2.http.*

interface RecipeApiService {

    @GET("recipes")
    suspend fun getAllRecipes(): Response<List<Recipe>>

    @GET("recipe/{id}")
    suspend fun getRecipeById(@Path("id") id: Int): Response<Recipe>

    @POST("recipe")
    suspend fun createRecipe(@Body recipe: Recipe): Response<Recipe>

    @DELETE("recipe/{id}")
    suspend fun deleteRecipe(@Path("id") id: Int): Response<Unit>

    @GET("allRecipes")
    suspend fun getAllRecipesComplete(): Response<List<Recipe>>
}
