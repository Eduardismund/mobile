package com.example.examprep.repository

import android.util.Log
import com.example.examprep.database.RecipeDao
import com.example.examprep.model.Recipe
import com.example.examprep.network.RetrofitClient
import retrofit2.Response

class RecipeRepository(private val recipeDao: RecipeDao) {

    private val apiService = RetrofitClient.apiService
    private val TAG = "RecipeRepository"

    suspend fun getAllRecipes(forceRefresh: Boolean = false): List<Recipe> {
        return try {
            Log.d(TAG, "Fetching recipes from network")
            val response = apiService.getAllRecipes()
            if (response.isSuccessful) {
                val recipes = response.body() ?: emptyList()
                // Cache to database
                recipeDao.insertAll(recipes)
                Log.d(TAG, "Cached ${recipes.size} recipes to database")
                recipes
            } else {
                Log.e(TAG, "Network error: ${response.code()}")
                // Return cached data on error
                getCachedRecipes()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching recipes: ${e.message}")
            // Return cached data on exception
            getCachedRecipes()
        }
    }

    suspend fun getCachedRecipes(): List<Recipe> {
        Log.d(TAG, "Fetching recipes from local database")
        return recipeDao.getAllRecipes()
    }

    suspend fun getRecipeById(id: Int): Recipe? {
        return try {
            Log.d(TAG, "Fetching recipe $id from network")
            val response = apiService.getRecipeById(id)
            if (response.isSuccessful) {
                val recipe = response.body()
                recipe?.let {
                    recipeDao.insertRecipe(it)
                    Log.d(TAG, "Cached recipe $id to database")
                }
                recipe
            } else {
                Log.e(TAG, "Network error: ${response.code()}")
                recipeDao.getRecipeById(id)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching recipe: ${e.message}")
            recipeDao.getRecipeById(id)
        }
    }

    suspend fun createRecipe(recipe: Recipe): Response<Recipe> {
        Log.d(TAG, "Creating recipe: ${recipe.title}")
        return apiService.createRecipe(recipe)
    }

    suspend fun deleteRecipe(id: Int): Response<Unit> {
        Log.d(TAG, "Deleting recipe: $id")
        val response = apiService.deleteRecipe(id)
        if (response.isSuccessful) {
            recipeDao.deleteById(id)
            Log.d(TAG, "Deleted recipe $id from local database")
        }
        return response
    }

    suspend fun deleteRecipeFromCache(id: Int) {
        Log.d(TAG, "Deleting recipe $id from local cache only (not found on server)")
        recipeDao.deleteById(id)
    }

    suspend fun cacheRecipe(recipe: Recipe) {
        Log.d(TAG, "Caching recipe ${recipe.id} to local database")
        recipeDao.insertRecipe(recipe)
    }

    suspend fun getAllRecipesComplete(): List<Recipe> {
        return try {
            Log.d(TAG, "Fetching all recipes (complete) from network")
            val response = apiService.getAllRecipesComplete()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.e(TAG, "Network error: ${response.code()}")
                getCachedRecipes()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching all recipes: ${e.message}")
            getCachedRecipes()
        }
    }
}
