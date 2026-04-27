package com.example.easebudgetv1.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.easebudgetv1.R
import com.example.easebudgetv1.data.database.EaseBudgetDatabase
import com.example.easebudgetv1.data.repository.EaseBudgetRepository
import com.example.easebudgetv1.ui.adapters.CategoryBudgetAdapter
import com.example.easebudgetv1.utils.CurrencyUtils
import com.example.easebudgetv1.utils.DateUtils
import com.example.easebudgetv1.utils.SessionManager
import com.example.easebudgetv1.viewmodel.BudgetViewModel
import com.example.easebudgetv1.viewmodel.BudgetViewModelFactory
import com.google.android.material.textfield.TextInputEditText

// (Author, 2024) Budget fragment for managing monthly budgets and category limits
class BudgetFragment : Fragment() {
    
    private lateinit var budgetViewModel: BudgetViewModel
    private lateinit var categoryBudgetAdapter: CategoryBudgetAdapter
    private lateinit var sessionManager: SessionManager
    
    // Views
    private lateinit var monthlyBudgetAmount: TextView
    private lateinit var monthlyBudgetSubtitle: TextView
    private lateinit var editMonthlyBudget: ImageView
    private lateinit var categoryLimitsRecyclerView: RecyclerView
    private lateinit var noCategoryLimitsTextView: TextView
    private lateinit var addCategoryLimit: ImageView
    private lateinit var totalBudgetAmount: TextView
    private lateinit var categoryLimitsTotal: TextView
    private lateinit var readyToAssignSummary: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize ViewModel
        val database = EaseBudgetDatabase.getDatabase(requireContext())
        val repository = EaseBudgetRepository(
            database.userDao(),
            database.categoryDao(),
            database.transactionDao(),
            database.budgetGoalDao()
        )
        sessionManager = SessionManager(requireContext())
        val currentUserId = sessionManager.getUserId()
        
        budgetViewModel = viewModels<BudgetViewModel> {
            BudgetViewModelFactory(repository, currentUserId)
        }.value
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_budget, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }
    
    private fun initializeViews(view: View) {
        monthlyBudgetAmount = view.findViewById(R.id.monthlyBudgetAmount)
        monthlyBudgetSubtitle = view.findViewById(R.id.monthlyBudgetSubtitle)
        editMonthlyBudget = view.findViewById(R.id.editMonthlyBudget)
        categoryLimitsRecyclerView = view.findViewById(R.id.categoryLimitsRecyclerView)
        noCategoryLimitsTextView = view.findViewById(R.id.noCategoryLimitsTextView)
        addCategoryLimit = view.findViewById(R.id.addCategoryLimit)
        totalBudgetAmount = view.findViewById(R.id.totalBudgetAmount)
        categoryLimitsTotal = view.findViewById(R.id.categoryLimitsTotal)
        readyToAssignSummary = view.findViewById(R.id.readyToAssignSummary)
    }
    
    private fun setupRecyclerView() {
        categoryBudgetAdapter = CategoryBudgetAdapter { category ->
            showEditCategoryLimitDialog(category)
        }
        
        categoryLimitsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryBudgetAdapter
        }
    }
    
    private fun setupObservers() {
        // Observe budget goal
        budgetViewModel.budgetGoal.observe(viewLifecycleOwner, Observer { budgetGoal ->
            if (budgetGoal != null) {
                monthlyBudgetAmount.text = CurrencyUtils.formatCurrency(budgetGoal.monthlyTotalBudget)
                monthlyBudgetSubtitle.text = "For ${DateUtils.getMonthName(budgetGoal.month)} ${budgetGoal.year}"
                totalBudgetAmount.text = CurrencyUtils.formatCurrency(budgetGoal.monthlyTotalBudget)
            } else {
                monthlyBudgetAmount.text = CurrencyUtils.formatCurrency(0.0)
                monthlyBudgetSubtitle.text = "No budget set"
                totalBudgetAmount.text = CurrencyUtils.formatCurrency(0.0)
            }
        })
        
        // Observe category spending with info
        budgetViewModel.categorySpending.observe(viewLifecycleOwner, Observer { spendingList ->
            if (spendingList == null || spendingList.isEmpty()) {
                categoryLimitsRecyclerView.visibility = View.GONE
                noCategoryLimitsTextView.visibility = View.VISIBLE
                categoryLimitsTotal.text = CurrencyUtils.formatCurrency(0.0)
            } else {
                categoryLimitsRecyclerView.visibility = View.VISIBLE
                noCategoryLimitsTextView.visibility = View.GONE
                categoryBudgetAdapter.submitList(spendingList)
                
                // Update category limits total
                val totalLimits = spendingList.sumOf { it.category.monthlyLimit }
                categoryLimitsTotal.text = CurrencyUtils.formatCurrency(totalLimits)
            }
        })

        // Keep categories active for selection dialogs
        budgetViewModel.categories.observe(viewLifecycleOwner) { }
        
        // Observe ready to assign
        budgetViewModel.readyToAssign.observe(viewLifecycleOwner, Observer { amount ->
            readyToAssignSummary.text = CurrencyUtils.formatCurrency(amount)
        })
        
        // Observe budget state
        budgetViewModel.budgetState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is com.example.easebudgetv1.viewmodel.BudgetState.Success -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    budgetViewModel.resetBudgetState()
                }
                is com.example.easebudgetv1.viewmodel.BudgetState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    budgetViewModel.resetBudgetState()
                }
                else -> {}
            }
        })
    }
    
    private fun setupClickListeners() {
        editMonthlyBudget.setOnClickListener {
            showEditMonthlyBudgetDialog()
        }
        
        addCategoryLimit.setOnClickListener {
            showAddCategoryLimitDialog()
        }
    }
    
    private fun showEditMonthlyBudgetDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_category_limit, null)
        val titleEditText = dialogView.findViewById<TextInputEditText>(R.id.titleEditText)
        val amountEditText = dialogView.findViewById<TextInputEditText>(R.id.amountEditText)
        
        titleEditText.setText("Monthly Budget")
        titleEditText.isEnabled = false
        
        // Set current budget amount
        val currentBudget = budgetViewModel.budgetGoal.value?.monthlyTotalBudget ?: 0.0
        amountEditText.setText(CurrencyUtils.formatCurrencyWithoutSymbol(currentBudget))
        
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Monthly Budget")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val amountStr = amountEditText.text?.toString() ?: ""
                val amount = CurrencyUtils.parseCurrency(amountStr) ?: 0.0
                
                if (amount > 0.0) {
                    val calendar = java.util.Calendar.getInstance()
                    budgetViewModel.createOrUpdateBudgetGoal(
                        amount, 0.0, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH)
                    )
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showAddCategoryLimitDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_category_limit, null)
        val titleEditText = dialogView.findViewById<TextInputEditText>(R.id.titleEditText)
        val amountEditText = dialogView.findViewById<TextInputEditText>(R.id.amountEditText)
        
        titleEditText.setText("Category Limit")
        titleEditText.isEnabled = false
        
        AlertDialog.Builder(requireContext())
            .setTitle("Add Category Limit")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                showCategorySelectionDialog(amountEditText.text?.toString())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showCategorySelectionDialog(amountStr: String?) {
        val categories = budgetViewModel.categories.value ?: emptyList()
        val categoryNames = categories.map { it.name }.toTypedArray()
        
        AlertDialog.Builder(requireContext())
            .setTitle("Select Category")
            .setItems(categoryNames) { _, which ->
                val selectedCategory = categories[which]
                val amount = CurrencyUtils.parseCurrency(amountStr ?: "") ?: 0.0
                
                if (amount > 0.0) {
                    budgetViewModel.updateCategoryLimit(selectedCategory.id, amount)
                }
            }
            .show()
    }
    
    private fun showEditCategoryLimitDialog(category: com.example.easebudgetv1.data.database.entities.Category) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_category_limit, null)
        val titleEditText = dialogView.findViewById<TextInputEditText>(R.id.titleEditText)
        val amountEditText = dialogView.findViewById<TextInputEditText>(R.id.amountEditText)
        
        titleEditText.setText(category.name)
        titleEditText.isEnabled = false
        amountEditText.setText(CurrencyUtils.formatCurrencyWithoutSymbol(category.monthlyLimit))
        
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Category Limit")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val amountStr = amountEditText.text?.toString() ?: ""
                val amount = CurrencyUtils.parseCurrency(amountStr) ?: 0.0
                
                if (amount > 0.0) {
                    budgetViewModel.updateCategoryLimit(category.id, amount)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
