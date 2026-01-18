package com.example.examprep.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.examprep.MainActivity
import com.example.examprep.R
import com.example.examprep.viewmodel.RecipeViewModel
import com.google.android.material.appbar.MaterialToolbar

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: RecipeViewModel
    private lateinit var toolbar: MaterialToolbar
    private lateinit var progressBar: ProgressBar
    private lateinit var tvTitle: TextView
    private lateinit var tvCategory: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvRating: TextView
    private lateinit var tvIngredients: TextView
    private lateinit var btnEdit: Button
    private lateinit var btnDelete: Button

    private var recipeId: Int = -1
    private var recipe: com.example.examprep.model.Recipe? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        // Get recipe from intent (full data - no network call needed!)
        recipe = intent.getParcelableExtra("RECIPE")

        if (recipe != null) {
            recipeId = recipe!!.id
            Log.d("RecipeDetailActivity", "Opening recipe detail: ${recipe!!.title}")
        } else {
            // Fallback: try to get by ID (legacy support)
            recipeId = intent.getIntExtra("RECIPE_ID", -1)
            if (recipeId == -1) {
                Toast.makeText(this, "Error: Recipe not found", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            Log.d("RecipeDetailActivity", "Opening recipe detail for ID: $recipeId")
        }

        // Initialize views
        toolbar = findViewById(R.id.toolbar)
        progressBar = findViewById(R.id.progressBar)
        tvTitle = findViewById(R.id.tvTitle)
        tvCategory = findViewById(R.id.tvCategory)
        tvDate = findViewById(R.id.tvDate)
        tvRating = findViewById(R.id.tvRating)
        tvIngredients = findViewById(R.id.tvIngredients)
        btnEdit = findViewById(R.id.btnEdit)
        btnDelete = findViewById(R.id.btnDelete)

        // Setup toolbar
        toolbar.setNavigationOnClickListener { finish() }

        // Setup ViewModel
        viewModel = ViewModelProvider(this)[RecipeViewModel::class.java]

        // If recipe was passed directly, display it immediately (no network call!)
        if (recipe != null) {
            displayRecipe(recipe!!)
        } else {
            // Otherwise fetch from ViewModel (fallback for legacy ID-based approach)
            viewModel.recipe.observe(this) { fetchedRecipe ->
                fetchedRecipe?.let {
                    displayRecipe(it)
                }
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe errors
        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        // Observe operation success
        viewModel.operationSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Recipe deleted successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // Setup button listeners
        btnEdit.setOnClickListener {
            val intent = Intent(this, AddEditRecipeActivity::class.java)
            intent.putExtra("RECIPE_ID", recipeId)
            startActivity(intent)
        }

        btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }

        // Load recipe data only if not passed directly
        if (recipe == null) {
            viewModel.getRecipeById(recipeId)
        }
    }

    private fun displayRecipe(recipe: com.example.examprep.model.Recipe) {
        tvTitle.text = recipe.title
        tvCategory.text = getCategoryDisplayName(recipe.category)
        tvDate.text = formatDate(recipe.date)
        tvRating.text = String.format("%.1f", recipe.rating)
        tvIngredients.text = recipe.ingredients

        // Set category color
        val categoryColor = when (recipe.category.lowercase()) {
            "main course" -> getColor(R.color.main_course)
            "dessert" -> getColor(R.color.dessert)
            "appetizer" -> getColor(R.color.appetizer)
            "beverage" -> getColor(R.color.beverage)
            else -> getColor(R.color.primary)
        }
        tvCategory.setBackgroundColor(categoryColor)
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Recipe")
            .setMessage("Are you sure you want to delete this recipe?")
            .setPositiveButton("Delete") { _, _ ->
                Log.d("RecipeDetailActivity", "Deleting recipe ID: $recipeId")
                viewModel.deleteRecipe(recipeId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun formatDate(date: String): String {
        return try {
            val parts = date.split("-")
            if (parts.size == 3) {
                val year = parts[0]
                val month = getMonthName(parts[1].toInt())
                val day = parts[2].toInt()
                "$month $day, $year"
            } else {
                date
            }
        } catch (e: Exception) {
            date
        }
    }

    private fun getMonthName(month: Int): String {
        return when (month) {
            1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"
            5 -> "May"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Aug"
            9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; 12 -> "Dec"
            else -> month.toString()
        }
    }

    private fun getCategoryDisplayName(category: String): String {
        return when (category.lowercase()) {
            "main course" -> "MAIN COURSE"
            "dessert" -> "DESSERT"
            "appetizer" -> "APPETIZER"
            "beverage" -> "BEVERAGE"
            else -> category.uppercase()
        }
    }
}
