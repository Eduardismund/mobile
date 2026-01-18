package com.example.examprep.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.examprep.R
import com.example.examprep.model.Recipe
import com.example.examprep.viewmodel.RecipeViewModel
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.*

class AddEditRecipeActivity : AppCompatActivity() {

    private lateinit var viewModel: RecipeViewModel
    private lateinit var toolbar: MaterialToolbar
    private lateinit var progressBar: ProgressBar
    private lateinit var etTitle: EditText
    private lateinit var etIngredients: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var etRating: EditText
    private lateinit var etDate: EditText
    private lateinit var btnSave: Button

    private var recipeId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_recipe)

        // Check if editing existing recipe
        recipeId = intent.getIntExtra("RECIPE_ID", -1).takeIf { it != -1 }

        // Initialize views
        toolbar = findViewById(R.id.toolbar)
        progressBar = findViewById(R.id.progressBar)
        etTitle = findViewById(R.id.etTitle)
        etIngredients = findViewById(R.id.etIngredients)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        etRating = findViewById(R.id.etRating)
        etDate = findViewById(R.id.etDate)
        btnSave = findViewById(R.id.btnSave)

        // Setup toolbar
        toolbar.title = if (recipeId != null) "Edit Recipe" else "Add Recipe"
        toolbar.setNavigationOnClickListener { finish() }

        // Setup category spinner
        val categories = arrayOf("Main Course", "Dessert", "Appetizer", "Beverage")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        // Set today's date as default
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        etDate.setText(sdf.format(Date()))

        // Setup ViewModel
        viewModel = ViewModelProvider(this)[RecipeViewModel::class.java]

        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnSave.isEnabled = !isLoading
        }

        // Observe errors
        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                // Show error in both Toast and AlertDialog for critical operations
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()

                // If it's a connection error, show dialog with retry option
                if (it.contains("Server is offline") ||
                    it.contains("Cannot connect") ||
                    it.contains("Network error") ||
                    it.contains("timeout")) {
                    showConnectionErrorDialog(it)
                }

                viewModel.clearError()
            }
        }

        // Observe operation success
        viewModel.operationSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Recipe saved successfully", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
        }

        // Save button click
        btnSave.setOnClickListener {
            saveRecipe()
        }

        // Set placeholder data for create mode
        setPlaceholderData()
    }

    private fun saveRecipe() {
        // Validate inputs
        val title = etTitle.text.toString().trim()
        val ingredients = etIngredients.text.toString().trim()
        val category = spinnerCategory.selectedItem.toString()
        val ratingStr = etRating.text.toString().trim()
        val date = etDate.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
            return
        }

        if (ingredients.isEmpty()) {
            Toast.makeText(this, "Please enter ingredients", Toast.LENGTH_SHORT).show()
            return
        }

        if (ratingStr.isEmpty()) {
            Toast.makeText(this, "Please enter a rating", Toast.LENGTH_SHORT).show()
            return
        }

        val rating = try {
            ratingStr.toDouble()
        } catch (e: Exception) {
            Toast.makeText(this, "Invalid rating format", Toast.LENGTH_SHORT).show()
            return
        }

        if (rating < 0 || rating > 5) {
            Toast.makeText(this, "Rating must be between 0 and 5", Toast.LENGTH_SHORT).show()
            return
        }

        if (date.isEmpty() || !date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            Toast.makeText(this, "Invalid date format. Use YYYY-MM-DD", Toast.LENGTH_SHORT).show()
            return
        }

        // Create recipe (ID will be generated by server)
        val recipe = Recipe(
            id = 0, // Server will generate ID
            title = title,
            ingredients = ingredients,
            category = category,
            rating = rating,
            date = date
        )

        Log.d("AddEditRecipeActivity", "Creating recipe: $title")
        viewModel.createRecipe(recipe)
    }

    private fun showConnectionErrorDialog(errorMessage: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("❌ Server Connection Error")
            .setMessage("$errorMessage\n\nYour recipe data is safe. You can:\n• Check if the server is running\n• Try again when the server is online")
            .setPositiveButton("Retry") { _, _ ->
                // Retry saving the recipe
                saveRecipe()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun setPlaceholderData() {
        // Set example data for create mode to help user understand format
        etTitle.setText("Grilled Chicken Salad")
        etIngredients.setText("Chicken breast, Lettuce, Tomatoes, Cucumber, Olive oil, Lemon")
        spinnerCategory.setSelection(0) // Main Course
        etRating.setText("4.5")
    }
}
