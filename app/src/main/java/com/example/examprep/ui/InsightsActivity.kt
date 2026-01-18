package com.example.examprep.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.examprep.R
import com.example.examprep.model.Recipe
import com.example.examprep.viewmodel.RecipeViewModel
import com.google.android.material.appbar.MaterialToolbar

class InsightsActivity : AppCompatActivity() {

    private lateinit var viewModel: RecipeViewModel
    private lateinit var toolbar: MaterialToolbar
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CategoryRatingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insights)

        toolbar = findViewById(R.id.toolbar)
        progressBar = findViewById(R.id.progressBar)
        recyclerView = findViewById(R.id.recyclerView)

        toolbar.setNavigationOnClickListener { finish() }

        adapter = CategoryRatingAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(this)[RecipeViewModel::class.java]

        viewModel.getAllRecipesForAnalysis().observe(this) { recipes ->
            Log.d("InsightsActivity", "Received ${recipes.size} recipes for analysis")
            val topCategories = calculateTopCategories(recipes)
            adapter.updateData(topCategories)
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        progressBar.visibility = View.VISIBLE
    }

    private fun calculateTopCategories(recipes: List<Recipe>): List<CategoryRating> {
        val categoryMap = recipes.groupBy { it.category }

        return categoryMap.map { (category, recipesInCategory) ->
            val totalRating = recipesInCategory.sumOf { it.rating }
            val avgRating = recipesInCategory.map { it.rating }.average()
            CategoryRating(category, recipesInCategory.size, totalRating, avgRating)
        }.sortedByDescending { it.totalRating }.take(3)
    }

    data class CategoryRating(
        val category: String,
        val count: Int,
        val totalRating: Double,
        val avgRating: Double
    )

    inner class CategoryRatingAdapter(private var data: List<CategoryRating>) :
        RecyclerView.Adapter<CategoryRatingAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvRank: TextView = view.findViewById(R.id.tvRank)
            val tvCategory: TextView = view.findViewById(R.id.tvCategory)
            val tvTotalRating: TextView = view.findViewById(R.id.tvTotalRating)
            val tvAvgRating: TextView = view.findViewById(R.id.tvAvgRating)
            val tvCount: TextView = view.findViewById(R.id.tvCount)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_category_rating, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = data[position]
            holder.tvRank.text = (position + 1).toString()
            holder.tvCategory.text = item.category
            holder.tvTotalRating.text = String.format("%.1f", item.totalRating)
            holder.tvAvgRating.text = String.format("%.1f", item.avgRating)
            holder.tvCount.text = item.count.toString()

            // Set rank badge color
            val color = when (position) {
                0 -> holder.itemView.context.getColor(R.color.main_course)
                1 -> holder.itemView.context.getColor(R.color.rating_star)
                2 -> holder.itemView.context.getColor(R.color.appetizer)
                else -> holder.itemView.context.getColor(R.color.primary)
            }
            holder.tvRank.setBackgroundColor(color)
        }

        override fun getItemCount() = data.size

        fun updateData(newData: List<CategoryRating>) {
            data = newData
            notifyDataSetChanged()
            progressBar.visibility = View.GONE
        }
    }
}
