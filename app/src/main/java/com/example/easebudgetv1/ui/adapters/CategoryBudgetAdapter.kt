package com.example.easebudgetv1.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.easebudgetv1.R
import com.example.easebudgetv1.data.database.entities.Category
import com.example.easebudgetv1.utils.CurrencyUtils
import com.example.easebudgetv1.viewmodel.CategorySpendingWithInfo

// (Author, 2024) RecyclerView adapter for displaying category budget progress
class CategoryBudgetAdapter(
    private val onCategoryClick: (Category) -> Unit
) : ListAdapter<CategorySpendingWithInfo, CategoryBudgetAdapter.CategoryBudgetViewHolder>(CategoryBudgetDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryBudgetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_budget, parent, false)
        return CategoryBudgetViewHolder(view, onCategoryClick)
    }
    
    override fun onBindViewHolder(holder: CategoryBudgetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class CategoryBudgetViewHolder(
        itemView: View,
        private val onCategoryClick: (Category) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val categoryColorIndicator: View = itemView.findViewById(R.id.categoryColorIndicator)
        private val categoryNameTextView: android.widget.TextView = itemView.findViewById(R.id.categoryNameTextView)
        private val budgetAmountTextView: android.widget.TextView = itemView.findViewById(R.id.budgetAmountTextView)
        private val progressBar: android.widget.ProgressBar = itemView.findViewById(R.id.progressBar)
        private val spentTextView: android.widget.TextView = itemView.findViewById(R.id.spentTextView)
        private val remainingTextView: android.widget.TextView = itemView.findViewById(R.id.remainingTextView)
        
        fun bind(info: CategorySpendingWithInfo) {
            val category = info.category
            
            // Set category name
            categoryNameTextView.text = category.name
            
            // Set budget amount
            budgetAmountTextView.text = CurrencyUtils.formatCurrency(category.monthlyLimit)
            
            // Set category color indicator
            try {
                val color = android.graphics.Color.parseColor(category.colorHex)
                categoryColorIndicator.setBackgroundColor(color)
            } catch (e: Exception) {
                categoryColorIndicator.setBackgroundColor(itemView.context.getColor(R.color.accent_teal))
            }
            
            // Set progress bar
            progressBar.progress = info.percentage.toInt()
            
            // Set spending text
            spentTextView.text = "Spent: ${CurrencyUtils.formatCurrency(info.spent)}"
            
            // Set remaining text with color based on amount
            remainingTextView.text = "${CurrencyUtils.formatCurrency(info.remaining)} left"
            if (info.remaining < 0) {
                remainingTextView.setTextColor(itemView.context.getColor(R.color.warning_coral))
            } else {
                remainingTextView.setTextColor(itemView.context.getColor(R.color.text_secondary))
            }
            
            // Set click listener
            itemView.setOnClickListener {
                onCategoryClick(category)
            }
        }
    }
    
    private class CategoryBudgetDiffCallback : DiffUtil.ItemCallback<CategorySpendingWithInfo>() {
        override fun areItemsTheSame(oldItem: CategorySpendingWithInfo, newItem: CategorySpendingWithInfo): Boolean {
            return oldItem.category.id == newItem.category.id
        }
        
        override fun areContentsTheSame(oldItem: CategorySpendingWithInfo, newItem: CategorySpendingWithInfo): Boolean {
            return oldItem == newItem
        }
    }
}
