package com.example.examprep.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.examprep.database.AppDatabase
import com.example.examprep.model.Recipe
import com.example.examprep.repository.RecipeRepository
import kotlinx.coroutines.launch

class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RecipeRepository

    init {
        val recipeDao = AppDatabase.getDatabase(application).recipeDao()
        repository = RecipeRepository(recipeDao)
    }

    private val _recipes = MutableLiveData<List<Recipe>>()
    val recipes: LiveData<List<Recipe>> = _recipes

    private val _recipe = MutableLiveData<Recipe?>()
    val recipe: LiveData<Recipe?> = _recipe

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _operationSuccess = MutableLiveData<Boolean>()
    val operationSuccess: LiveData<Boolean> = _operationSuccess

    fun loadRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val recipesList = repository.getAllRecipes()
                _recipes.value = recipesList
                if (recipesList.isEmpty()) {
                    _errorMessage.value = "No recipes found. Pull to refresh or check connection."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading recipes: ${e.message}"
                // Try to load cached recipes
                val cached = repository.getCachedRecipes()
                _recipes.value = cached
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadRecipesFromCache() {
        viewModelScope.launch {
            // Load from local database only (no network call)
            val cached = repository.getCachedRecipes()
            _recipes.value = cached
        }
    }

    fun getRecipeById(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val recipeData = repository.getRecipeById(id)
                _recipe.value = recipeData
            } catch (e: Exception) {
                _errorMessage.value = "Error loading recipe: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createRecipe(recipe: Recipe) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.createRecipe(recipe)
                if (response.isSuccessful) {
                    _operationSuccess.value = true
                    // No need to call loadRecipes() - WebSocket will notify MainActivity
                    // and trigger the update automatically
                } else {
                    _errorMessage.value = "❌ Failed to create recipe: ${response.message()}"
                    _operationSuccess.value = false
                }
            } catch (e: Exception) {
                // Provide more explicit error messages based on exception type
                val errorMsg = when {
                    e is java.net.ConnectException ||
                    e.message?.contains("Failed to connect", ignoreCase = true) == true ||
                    e.message?.contains("Connection refused", ignoreCase = true) == true ||
                    e.message?.contains("Connection reset", ignoreCase = true) == true ->
                        "⚠️ Server is offline. Please make sure the server is running on http://192.168.1.194:2528 and try again."
                    e.message?.contains("timeout", ignoreCase = true) == true ->
                        "⏱️ Server timeout. The server took too long to respond. Please check your connection and try again."
                    e is java.net.UnknownHostException ->
                        "🌐 Network error. Cannot reach the server. Please check your network connection."
                    else ->
                        "❌ Error creating recipe: ${e.message ?: "Unknown error occurred. Please check if the server is running."}"
                }
                _errorMessage.value = errorMsg
                _operationSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteRecipe(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.deleteRecipe(id)
                if (response.isSuccessful) {
                    // Remove from local list without server fetch
                    val currentList = _recipes.value ?: emptyList()
                    _recipes.value = currentList.filter { it.id != id }
                    _operationSuccess.value = true
                } else if (response.code() == 404) {
                    // Recipe not found on server - already deleted or never existed
                    // Delete from local cache and update UI
                    repository.deleteRecipeFromCache(id)
                    val currentList = _recipes.value ?: emptyList()
                    _recipes.value = currentList.filter { it.id != id }
                    _operationSuccess.value = true
                } else {
                    _errorMessage.value = "❌ Failed to delete recipe: ${response.message()}"
                    _operationSuccess.value = false
                }
            } catch (e: Exception) {
                // Provide more explicit error messages based on exception type
                val errorMsg = when {
                    e is java.net.ConnectException ||
                    e.message?.contains("Failed to connect", ignoreCase = true) == true ||
                    e.message?.contains("Connection refused", ignoreCase = true) == true ||
                    e.message?.contains("Connection reset", ignoreCase = true) == true ->
                        "⚠️ Server is offline. Please make sure the server is running to delete recipes."
                    e.message?.contains("timeout", ignoreCase = true) == true ->
                        "⏱️ Server timeout. The server took too long to respond."
                    e is java.net.UnknownHostException ->
                        "🌐 Network error. Cannot reach the server."
                    else ->
                        "❌ Error deleting recipe: ${e.message ?: "Unknown error occurred."}"
                }
                _errorMessage.value = errorMsg
                _operationSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getAllRecipesForAnalysis(): LiveData<List<Recipe>> {
        val result = MutableLiveData<List<Recipe>>()
        viewModelScope.launch {
            try {
                // Use cached data for analysis to avoid unnecessary network calls
                val recipes = repository.getCachedRecipes()
                if (recipes.isEmpty()) {
                    // Only fetch from network if cache is empty
                    val fetchedRecipes = repository.getAllRecipesComplete()
                    result.value = fetchedRecipes
                } else {
                    result.value = recipes
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading recipes: ${e.message}"
            }
        }
        return result
    }

    fun addRecipeToList(recipe: Recipe) {
        // Add to UI list immediately (on main thread)
        val currentList = _recipes.value ?: emptyList()
        _recipes.value = listOf(recipe) + currentList

        // Cache to database for offline access (background thread)
        viewModelScope.launch {
            repository.cacheRecipe(recipe)
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun resetOperationSuccess() {
        _operationSuccess.value = false
    }
}
