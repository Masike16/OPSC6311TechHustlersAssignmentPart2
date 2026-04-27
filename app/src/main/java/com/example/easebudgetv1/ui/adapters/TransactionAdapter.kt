package com.example.easebudgetv1.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.easebudgetv1.R
import com.example.easebudgetv1.data.database.entities.Transaction
import com.example.easebudgetv1.data.database.entities.TransactionType
import com.example.easebudgetv1.utils.CurrencyUtils
import com.example.easebudgetv1.utils.DateUtils
import java.io.File

// (Author, 2024) RecyclerView adapter for displaying transactions
class TransactionAdapter(
    private val onTransactionClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view, onTransactionClick)
    }
    
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class TransactionViewHolder(
        itemView: View,
        private val onTransactionClick: (Transaction) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val typeIndicator: View = itemView.findViewById(R.id.typeIndicator)
        private val receiptThumbnail: android.widget.ImageView = itemView.findViewById(R.id.receiptThumbnail)
        private val categoryTextView: android.widget.TextView = itemView.findViewById(R.id.categoryTextView)
        private val amountTextView: android.widget.TextView = itemView.findViewById(R.id.amountTextView)
        private val descriptionTextView: android.widget.TextView = itemView.findViewById(R.id.descriptionTextView)
        private val dateTextView: android.widget.TextView = itemView.findViewById(R.id.dateTextView)
        
        fun bind(transaction: Transaction) {
            // Set type indicator color
            val colorRes = if (transaction.type == TransactionType.INCOME) {
                R.color.success_green
            } else {
                R.color.warning_coral
            }
            typeIndicator.setBackgroundColor(itemView.context.getColor(colorRes))
            
            // Set category name placeholder
            categoryTextView.text = transaction.description // fallback
            
            // Set amount with sign
            val amountText = if (transaction.type == TransactionType.INCOME) {
                "+ ${CurrencyUtils.formatCurrency(transaction.amount)}"
            } else {
                "- ${CurrencyUtils.formatCurrency(transaction.amount)}"
            }
            amountTextView.text = amountText
            amountTextView.setTextColor(itemView.context.getColor(colorRes))
            
            // Set description
            descriptionTextView.text = transaction.description
            
            // Set date
            dateTextView.text = DateUtils.formatForDisplay(transaction.date)
            
            // Show receipt thumbnail if available
            if (transaction.receiptPath != null) {
                receiptThumbnail.visibility = View.VISIBLE
                val file = File(transaction.receiptPath)
                if (file.exists()) {
                    Glide.with(itemView.context)
                        .load(file)
                        .placeholder(R.drawable.ic_receipt)
                        .centerCrop()
                        .into(receiptThumbnail)
                } else {
                    receiptThumbnail.setImageResource(R.drawable.ic_receipt)
                }
            } else {
                receiptThumbnail.visibility = View.GONE
            }
            
            // Set click listener
            itemView.setOnClickListener {
                onTransactionClick(transaction)
            }
        }
    }
    
    private class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}
