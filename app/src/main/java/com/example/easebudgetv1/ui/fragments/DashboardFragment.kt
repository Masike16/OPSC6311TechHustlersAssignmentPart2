/**
 * Group: Tech Hustlers
 * Members:
 * - ST10451774 - Acazia Ammon
 * - ST10452404 - Masike Jr Rasenyalo
 * - ST10452409 - Liyema Masala
 */
package com.example.easebudgetv1.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.easebudgetv1.R
import com.example.easebudgetv1.data.database.EaseBudgetDatabase
import com.example.easebudgetv1.data.repository.EaseBudgetRepository
import com.example.easebudgetv1.ui.adapters.CategoryBudgetAdapter
import com.example.easebudgetv1.ui.adapters.TransactionAdapter
import com.example.easebudgetv1.utils.CurrencyUtils
import com.example.easebudgetv1.utils.DateUtils
import com.example.easebudgetv1.utils.SessionManager
import com.example.easebudgetv1.viewmodel.BudgetViewModel
import com.example.easebudgetv1.viewmodel.BudgetViewModelFactory
import com.example.easebudgetv1.viewmodel.TransactionViewModel
import com.example.easebudgetv1.viewmodel.TransactionViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView

// (Author, 2024) Dashboard fragment showing budget overview and recent transactions
class DashboardFragment : Fragment() {
    
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var budgetViewModel: BudgetViewModel
    private lateinit var sessionManager: SessionManager
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var categoryProgressAdapter: CategoryBudgetAdapter
    
    // Views
    private lateinit var greetingTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var readyToAssignAmount: TextView
    private lateinit var incomeAmount: TextView
    private lateinit var expensesAmount: TextView
    private lateinit var recentTransactionsRecyclerView: RecyclerView
    private lateinit var categoryProgressRecyclerView: RecyclerView
    private lateinit var noTransactionsTextView: TextView
    private lateinit var seeAllTransactions: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize ViewModels
        val database = EaseBudgetDatabase.getDatabase(requireContext())
        val repository = EaseBudgetRepository(
            database.userDao(),
            database.categoryDao(),
            database.transactionDao(),
            database.budgetGoalDao()
        )
        sessionManager = SessionManager(requireContext())
        val currentUserId = sessionManager.getUserId()
        
        transactionViewModel = viewModels<TransactionViewModel> {
            TransactionViewModelFactory(repository, currentUserId)
        }.value
        
        budgetViewModel = viewModels<BudgetViewModel> {
            BudgetViewModelFactory(repository, currentUserId)
        }.value
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupRecyclerViews()
        setupObservers()
        setupClickListeners()
        
        // Load initial data
        loadDashboardData()
    }
    
    private fun initializeViews(view: View) {
        greetingTextView = view.findViewById(R.id.greetingTextView)
        dateTextView = view.findViewById(R.id.dateTextView)
        readyToAssignAmount = view.findViewById(R.id.readyToAssignAmount)
        incomeAmount = view.findViewById(R.id.incomeAmount)
        expensesAmount = view.findViewById(R.id.expensesAmount)
        recentTransactionsRecyclerView = view.findViewById(R.id.recentTransactionsRecyclerView)
        categoryProgressRecyclerView = view.findViewById(R.id.categoryProgressRecyclerView)
        noTransactionsTextView = view.findViewById(R.id.noTransactionsTextView)
        seeAllTransactions = view.findViewById(R.id.seeAllTransactions)
    }
    
    private fun setupRecyclerViews() {
        // Recent Transactions
        transactionAdapter = TransactionAdapter { transaction ->
            val action = DashboardFragmentDirections.actionGlobalAddEditTransactionFragment(transaction.id)
            findNavController().navigate(action)
        }
        recentTransactionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
        }

        // Category Progress
        categoryProgressAdapter = CategoryBudgetAdapter { } // Read only on dashboard
        categoryProgressRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryProgressAdapter
        }
    }
    
    private fun setupObservers() {
        // Observe recent transactions
        transactionViewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            if (transactions.isNullOrEmpty()) {
                recentTransactionsRecyclerView.visibility = View.GONE
                noTransactionsTextView.visibility = View.VISIBLE
            } else {
                recentTransactionsRecyclerView.visibility = View.VISIBLE
                noTransactionsTextView.visibility = View.GONE
                transactionAdapter.submitList(transactions.take(5))
            }
        }
        
        // Observe ready to assign amount
        budgetViewModel.readyToAssign.observe(viewLifecycleOwner) { amount ->
            readyToAssignAmount.text = CurrencyUtils.formatCurrency(amount ?: 0.0)
        }
        
        // Observe category progress
        budgetViewModel.categorySpending.observe(viewLifecycleOwner) { spendingList ->
            categoryProgressAdapter.submitList(spendingList)
        }
        
        // Observe monthly income and expenses
        val (startOfMonth, endOfMonth) = DateUtils.getMonthRange()
        
        transactionViewModel.getTotalIncome(startOfMonth, endOfMonth).observe(viewLifecycleOwner) { income ->
            incomeAmount.text = CurrencyUtils.formatCurrency(income ?: 0.0)
        }
        
        transactionViewModel.getTotalExpenses(startOfMonth, endOfMonth).observe(viewLifecycleOwner) { expenses ->
            expensesAmount.text = CurrencyUtils.formatCurrency(expenses ?: 0.0)
        }
    }
    
    private fun setupClickListeners() {
        seeAllTransactions.setOnClickListener {
            // Update bottom navigation state manually to highlight Transactions tab
            activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)?.selectedItemId = R.id.navigation_transactions
        }
    }
    
    private fun loadDashboardData() {
        // Set greeting
        val username = sessionManager.getUsername() ?: "User"
        greetingTextView.text = "Welcome back, $username!"
        
        // Set current date
        dateTextView.text = DateUtils.formatForDisplay(System.currentTimeMillis())
        
        // Trigger data refresh
        transactionViewModel.setDateFilter(com.example.easebudgetv1.viewmodel.DateFilter.MONTH)
    }
    
    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }
}
