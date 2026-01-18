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

class ExploreActivity : AppCompatActivity() {

    private lateinit var viewModel: RecipeViewModel
    private lateinit var toolbar: MaterialToolbar
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MonthlyRatingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore)

        toolbar = findViewById(R.id.toolbar)
        progressBar = findViewById(R.id.progressBar)
        recyclerView = findViewById(R.id.recyclerView)

        toolbar.setNavigationOnClickListener { finish() }

        adapter = MonthlyRatingAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(this)[RecipeViewModel::class.java]

        viewModel.getAllRecipesForAnalysis().observe(this) { recipes ->
            Log.d("ExploreActivity", "Received ${recipes.size} recipes for analysis")
            val monthlyData = calculateMonthlyRatings(recipes)
            adapter.updateData(monthlyData)
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        progressBar.visibility = View.VISIBLE
        // ViewModel automatically loads data
    }

    private fun calculateMonthlyRatings(recipes: List<Recipe>): List<MonthlyRating> {
        val monthlyMap = recipes.groupBy { recipe ->
            recipe.date.substring(0, 7) // Get YYYY-MM
        }

        return monthlyMap.map { (month, recipesInMonth) ->
            val avgRating = recipesInMonth.map { it.rating }.average()
            MonthlyRating(month, recipesInMonth.size, avgRating)
        }.sortedByDescending { it.rating }
    }

    data class MonthlyRating(val month: String, val count: Int, val rating: Double)

    inner class MonthlyRatingAdapter(private var data: List<MonthlyRating>) :
        RecyclerView.Adapter<MonthlyRatingAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvMonth: TextView = view.findViewById(R.id.tvMonth)
            val tvCount: TextView = view.findViewById(R.id.tvCount)
            val tvRating: TextView = view.findViewById(R.id.tvRating)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_monthly_rating, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = data[position]
            holder.tvMonth.text = formatMonth(item.month)
            holder.tvCount.text = "${item.count} recipe${if (item.count != 1) "s" else ""}"
            holder.tvRating.text = String.format("%.1f", item.rating)
        }

        override fun getItemCount() = data.size

        fun updateData(newData: List<MonthlyRating>) {
            data = newData
            notifyDataSetChanged()
            progressBar.visibility = View.GONE
        }

        private fun formatMonth(monthStr: String): String {
            val parts = monthStr.split("-")
            if (parts.size != 2) return monthStr
            val year = parts[0]
            val month = when (parts[1]) {
                "01" -> "January"; "02" -> "February"; "03" -> "March"; "04" -> "April"
                "05" -> "May"; "06" -> "June"; "07" -> "July"; "08" -> "August"
                "09" -> "September"; "10" -> "October"; "11" -> "November"; "12" -> "December"
                else -> parts[1]
            }
            return "$month $year"
        }
    }
}
