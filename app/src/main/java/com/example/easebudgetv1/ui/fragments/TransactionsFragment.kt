package com.example.easebudgetv1.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.easebudgetv1.R
import com.example.easebudgetv1.data.database.EaseBudgetDatabase
import com.example.easebudgetv1.data.repository.EaseBudgetRepository
import com.example.easebudgetv1.ui.adapters.TransactionAdapter
import com.example.easebudgetv1.viewmodel.DateFilter
import com.example.easebudgetv1.viewmodel.TransactionViewModel
import com.example.easebudgetv1.viewmodel.TransactionViewModelFactory

// (Author, 2024) Transactions fragment showing list of all transactions with filtering
class TransactionsFragment : Fragment() {
    
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var transactionAdapter: TransactionAdapter
    
    // Views
    private lateinit var filterSpinner: Spinner
    private lateinit var transactionsRecyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var addFirstTransactionButton: Button
    
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
        val sessionManager = com.example.easebudgetv1.utils.SessionManager(requireContext())
        val currentUserId = sessionManager.getUserId()
        
        transactionViewModel = viewModels<TransactionViewModel> {
            TransactionViewModelFactory(repository, currentUserId)
        }.value
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupRecyclerView()
        setupSpinner()
        setupObservers()
        setupClickListeners()
    }
    
    private fun initializeViews(view: View) {
        filterSpinner = view.findViewById(R.id.filterSpinner)
        transactionsRecyclerView = view.findViewById(R.id.transactionsRecyclerView)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
        addFirstTransactionButton = view.findViewById(R.id.addFirstTransactionButton)
    }
    
    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter { transaction ->
            // Handle transaction click - navigate to edit
            val action = TransactionsFragmentDirections.actionGlobalAddEditTransactionFragment(transaction.id)
            findNavController().navigate(action)
        }
        
        transactionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
        }
    }
    
    private fun setupSpinner() {
        val filterOptions = arrayOf("Today", "This Week", "This Month", "Custom Range")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filterOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterSpinner.adapter = adapter
        
        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val filter = when (position) {
                    0 -> DateFilter.TODAY
                    1 -> DateFilter.WEEK
                    2 -> DateFilter.MONTH
                    3 -> DateFilter.CUSTOM
                    else -> DateFilter.MONTH
                }
                transactionViewModel.setDateFilter(filter)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun setupObservers() {
        // Observe transactions
        transactionViewModel.transactions.observe(viewLifecycleOwner, Observer { transactions ->
            if (transactions.isEmpty()) {
                transactionsRecyclerView.visibility = View.GONE
                emptyStateLayout.visibility = View.VISIBLE
            } else {
                transactionsRecyclerView.visibility = View.VISIBLE
                emptyStateLayout.visibility = View.GONE
                transactionAdapter.submitList(transactions)
            }
        })
        
        // Observe transaction state for success/error messages
        transactionViewModel.transactionState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is com.example.easebudgetv1.viewmodel.TransactionState.Success -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    transactionViewModel.resetTransactionState()
                }
                is com.example.easebudgetv1.viewmodel.TransactionState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    transactionViewModel.resetTransactionState()
                }
                else -> {}
            }
        })
    }
    
    private fun setupClickListeners() {
        addFirstTransactionButton.setOnClickListener {
            navigateToAddTransaction()
        }
    }
    
    private fun navigateToAddTransaction() {
        val action = TransactionsFragmentDirections.actionGlobalAddEditTransactionFragment()
        findNavController().navigate(action)
    }
}
