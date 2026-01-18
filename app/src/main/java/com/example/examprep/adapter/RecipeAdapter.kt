package com.example.examprep.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.examprep.R
import com.example.examprep.model.Recipe

class RecipeAdapter(
    private var recipes: List<Recipe>,
    private val onItemClick: (Recipe) -> Unit = {},
    private val onDeleteClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvRecipeTitle)
        val tvCategory: TextView = itemView.findViewById(R.id.tvRecipeCategory)
        val tvRating: TextView = itemView.findViewById(R.id.tvRecipeRating)
        val btnDelete: Button = itemView.findViewById(R.id.btnDeleteRecipe)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        val context = holder.itemView.context

        // Set title
        holder.tvTitle.text = recipe.title

        // Set category with shortened text
        holder.tvCategory.text = getCategoryShortName(recipe.category)

        // Set rating
        holder.tvRating.text = String.format("%.1f", recipe.rating)

        // Set category badge color based on category
        val categoryColor = when (recipe.category.lowercase()) {
            "main course" -> context.getColor(R.color.main_course)
            "dessert" -> context.getColor(R.color.dessert)
            "appetizer" -> context.getColor(R.color.appetizer)
            "beverage" -> context.getColor(R.color.beverage)
            else -> context.getColor(R.color.primary)
        }
        holder.tvCategory.setBackgroundColor(categoryColor)

        // Click on card to view details
        holder.itemView.setOnClickListener {
            onItemClick(recipe)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(recipe)
        }
    }

    private fun formatDate(date: String): String {
        return try {
            // Input format: "2025-01-15"
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
            1 -> "Jan"
            2 -> "Feb"
            3 -> "Mar"
            4 -> "Apr"
            5 -> "May"
            6 -> "Jun"
            7 -> "Jul"
            8 -> "Aug"
            9 -> "Sep"
            10 -> "Oct"
            11 -> "Nov"
            12 -> "Dec"
            else -> month.toString()
        }
    }

    private fun getCategoryShortName(category: String): String {
        return when (category.lowercase()) {
            "main course" -> "MAIN"
            "dessert" -> "DESSERT"
            "appetizer" -> "APPETIZER"
            "beverage" -> "DRINK"
            else -> category.uppercase()
        }
    }

    override fun getItemCount(): Int = recipes.size

    fun updateRecipes(newRecipes: List<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }
}
