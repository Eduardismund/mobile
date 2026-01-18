package com.example.examprep

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.examprep.adapter.RecipeAdapter
import com.example.examprep.ui.AddEditRecipeActivity
import com.example.examprep.ui.ExploreActivity
import com.example.examprep.ui.InsightsActivity
import com.example.examprep.ui.RecipeDetailActivity
import com.example.examprep.viewmodel.RecipeViewModel
import com.example.examprep.websocket.RecipeWebSocketClient

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var emptyState: LinearLayout
    private lateinit var btnRetry: Button
    private lateinit var btnAddRecipe: Button
    private lateinit var btnMonthlyRatings: Button
    private lateinit var btnTopCategories: Button
    private lateinit var viewModel: RecipeViewModel
    private lateinit var adapter: RecipeAdapter
    private var webSocketClient: RecipeWebSocketClient? = null
    private var isInitialLoad = true
    private var hasLoadedSuccessfully = false

    // Activity result launcher for AddEditRecipeActivity
    private val addRecipeLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // When returning from AddEditRecipeActivity, load recipes from cache
        // The WebSocket broadcast + cache save happens asynchronously,
        // so we give it a moment then load from cache
        if (result.resultCode == RESULT_OK) {
            Log.d("MainActivity", "Returned from AddEditRecipeActivity with success")
            // Small delay to allow WebSocket to process and cache the new recipe
            recyclerView.postDelayed({
                viewModel.loadRecipesFromCache()
            }, 300) // 300ms delay
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("MainActivity", "onCreate")

        // Handle window insets for bottom buttons to avoid overlap with navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottomButtons)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                systemBars.bottom + 8
            )
            insets
        }

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewRecipes)
        progressBar = findViewById(R.id.progressBar)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        emptyState = findViewById(R.id.emptyState)
        btnRetry = findViewById(R.id.btnRetry)
        btnAddRecipe = findViewById(R.id.btnAddRecipe)
        btnMonthlyRatings = findViewById(R.id.btnMonthlyRatings)
        btnTopCategories = findViewById(R.id.btnTopCategories)

        // Setup ViewModel
        viewModel = ViewModelProvider(this)[RecipeViewModel::class.java]

        // Setup RecyclerView with click listener
        adapter = RecipeAdapter(emptyList(),
            onItemClick = { recipe ->
                val intent = Intent(this, RecipeDetailActivity::class.java)
                intent.putExtra("RECIPE", recipe)
                startActivity(intent)
            },
            onDeleteClick = { recipe ->
                showDeleteConfirmation(recipe)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Button click listeners
        btnRetry.setOnClickListener {
            isInitialLoad = true
            viewModel.loadRecipes()
        }

        btnAddRecipe.setOnClickListener {
            addRecipeLauncher.launch(Intent(this, AddEditRecipeActivity::class.java))
        }

        btnMonthlyRatings.setOnClickListener {
            startActivity(Intent(this, ExploreActivity::class.java))
        }

        btnTopCategories.setOnClickListener {
            startActivity(Intent(this, InsightsActivity::class.java))
        }

        // Setup SwipeRefresh
        swipeRefresh.setColorSchemeResources(
            R.color.primary,
            R.color.accent,
            R.color.primary_dark
        )
        swipeRefresh.setOnRefreshListener {
            viewModel.loadRecipes()
        }

        // Observe recipes data
        viewModel.recipes.observe(this) { recipes ->
            adapter.updateRecipes(recipes)

            // Check if this is first successful load
            if (recipes.isNotEmpty() && isInitialLoad) {
                hasLoadedSuccessfully = true
                isInitialLoad = false
            }

            updateEmptyState(recipes.isEmpty())
        }

        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            if (!swipeRefresh.isRefreshing) {
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
            if (!isLoading) {
                swipeRefresh.isRefreshing = false
            }
        }

        // Observe error messages
        viewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                // On initial load with error, show more prominent message
                if (isInitialLoad && !hasLoadedSuccessfully) {
                    Toast.makeText(
                        this,
                        "⚠️ Cannot connect to server. Please check if the server is running and try again.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                }
                viewModel.clearError()
            }
        }

        // Observe operation success (for delete operations)
        viewModel.operationSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "✅ Recipe deleted successfully", Toast.LENGTH_SHORT).show()
                viewModel.resetOperationSuccess()
            }
        }

        // Load recipes only on initial load
        if (isInitialLoad) {
            viewModel.loadRecipes()
            isInitialLoad = false
        }

        // Setup WebSocket for real-time notifications
        setupWebSocket()
    }

    private fun setupWebSocket() {
        Log.d("MainActivity", "Setting up WebSocket connection...")

        webSocketClient = RecipeWebSocketClient { newRecipe ->
            runOnUiThread {
                Log.d("MainActivity", "🎉 New recipe received via WebSocket: ${newRecipe.title}")
                Toast.makeText(
                    this,
                    "🆕 New recipe added: ${newRecipe.title} (${newRecipe.category})",
                    Toast.LENGTH_LONG
                ).show()
                // Add to local list without server fetch
                viewModel.addRecipeToList(newRecipe)
            }
        }
        // Connect to WebSocket (use same IP as API)
        val wsUrl = "ws://192.168.1.194:2528"
        Log.d("MainActivity", "Connecting to WebSocket at: $wsUrl")
        webSocketClient?.connect(wsUrl)
    }

    private fun showDeleteConfirmation(recipe: com.example.examprep.model.Recipe) {
        AlertDialog.Builder(this)
            .setTitle("Delete Recipe")
            .setMessage("Are you sure you want to delete '${recipe.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                // Just call delete - success message will be shown by observer
                viewModel.deleteRecipe(recipe.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        // Don't reload here - we only load once at app start
        // Subsequent updates come from WebSocket or explicit refresh
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketClient?.disconnect()
    }
}
