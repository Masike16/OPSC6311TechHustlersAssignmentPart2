package com.example.easebudgetv1.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.easebudgetv1.R
import com.example.easebudgetv1.viewmodel.CategorySpendingData
import com.example.easebudgetv1.utils.CurrencyUtils

// (Author, 2024) RecyclerView adapter for displaying category spending in reports
class CategoryReportAdapter : ListAdapter<CategorySpendingData, CategoryReportAdapter.CategoryReportViewHolder>(CategoryReportDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report_category, parent, false)
        return CategoryReportViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: CategoryReportViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class CategoryReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        private val categoryColorView: View = itemView.findViewById(R.id.categoryColorView)
        private val categoryNameTextView: android.widget.TextView = itemView.findViewById(R.id.categoryNameTextView)
        private val amountTextView: android.widget.TextView = itemView.findViewById(R.id.amountTextView)
        
        fun bind(categorySpending: CategorySpendingData) {
            // Set category name
            categoryNameTextView.text = categorySpending.categoryName
            
            // Set amount
            amountTextView.text = CurrencyUtils.formatCurrency(categorySpending.amount)
            
            // Set category color
            try {
                val color = android.graphics.Color.parseColor(categorySpending.categoryColor)
                categoryColorView.setBackgroundColor(color)
            } catch (e: Exception) {
                categoryColorView.setBackgroundColor(itemView.context.getColor(R.color.accent_teal))
            }
        }
    }
    
    private class CategoryReportDiffCallback : DiffUtil.ItemCallback<CategorySpendingData>() {
        override fun areItemsTheSame(oldItem: CategorySpendingData, newItem: CategorySpendingData): Boolean {
            return oldItem.categoryId == newItem.categoryId
        }
        
        override fun areContentsTheSame(oldItem: CategorySpendingData, newItem: CategorySpendingData): Boolean {
            return oldItem == newItem
        }
    }
}
