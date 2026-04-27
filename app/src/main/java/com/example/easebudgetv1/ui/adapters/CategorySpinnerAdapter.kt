package com.example.easebudgetv1.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.easebudgetv1.R
import com.example.easebudgetv1.data.database.entities.Category

// (Author, 2024) Spinner adapter for category selection
class CategorySpinnerAdapter(private val context: Context) : BaseAdapter() {
    
    private var categories: List<Category> = emptyList()
    
    fun updateCategories(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }
    
    override fun getCount(): Int = categories.size
    
    override fun getItem(position: Int): Category = categories[position]
    
    override fun getItemId(position: Int): Long = categories[position].id
    
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_spinner_item, parent, false)
        
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = categories[position].name
        
        return view
    }
    
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_spinner_dropdown_item, parent, false)
        
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = categories[position].name
        
        return view
    }
    
    fun getItemId(item: Any?): Long {
        return when (item) {
            is Category -> item.id
            else -> 0
        }
    }
}
