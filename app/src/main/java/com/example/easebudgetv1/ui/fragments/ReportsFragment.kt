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
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.easebudgetv1.R
import com.example.easebudgetv1.data.database.EaseBudgetDatabase
import com.example.easebudgetv1.data.repository.EaseBudgetRepository
import com.example.easebudgetv1.ui.adapters.CategoryReportAdapter
import com.example.easebudgetv1.utils.CurrencyUtils
import com.example.easebudgetv1.utils.SessionManager
import com.example.easebudgetv1.viewmodel.ReportFilter
import com.example.easebudgetv1.viewmodel.ReportsViewModel
import com.example.easebudgetv1.viewmodel.ReportsViewModelFactory
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter

// (Author, 2024) Reports fragment showing spending analytics and charts
class ReportsFragment : Fragment() {
    
    private lateinit var reportsViewModel: ReportsViewModel
    private lateinit var categoryReportAdapter: CategoryReportAdapter
    private lateinit var sessionManager: SessionManager
    
    // Views
    private lateinit var reportsFilterSpinner: Spinner
    private lateinit var reportsIncomeAmount: TextView
    private lateinit var reportsExpensesAmount: TextView
    private lateinit var reportsBalanceAmount: TextView
    private lateinit var spendingPieChart: PieChart
    private lateinit var categoryLegendRecyclerView: RecyclerView
    private lateinit var noSpendingDataTextView: TextView
    private lateinit var topCategoriesRecyclerView: RecyclerView
    
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
        
        reportsViewModel = viewModels<ReportsViewModel> {
            ReportsViewModelFactory(repository, currentUserId)
        }.value
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reports, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupSpinner()
        setupRecyclerViews()
        setupPieChart()
        setupObservers()
    }
    
    private fun initializeViews(view: View) {
        reportsFilterSpinner = view.findViewById(R.id.reportsFilterSpinner)
        reportsIncomeAmount = view.findViewById(R.id.reportsIncomeAmount)
        reportsExpensesAmount = view.findViewById(R.id.reportsExpensesAmount)
        reportsBalanceAmount = view.findViewById(R.id.reportsBalanceAmount)
        spendingPieChart = view.findViewById(R.id.spendingPieChart)
        categoryLegendRecyclerView = view.findViewById(R.id.categoryLegendRecyclerView)
        noSpendingDataTextView = view.findViewById(R.id.noSpendingDataTextView)
        topCategoriesRecyclerView = view.findViewById(R.id.topCategoriesRecyclerView)
    }
    
    private fun setupSpinner() {
        val filterOptions = arrayOf("Today", "This Week", "This Month", "Custom Range")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filterOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        reportsFilterSpinner.adapter = adapter
        
        reportsFilterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val filter = when (position) {
                    0 -> ReportFilter.TODAY
                    1 -> ReportFilter.WEEK
                    2 -> ReportFilter.MONTH
                    3 -> ReportFilter.CUSTOM
                    else -> ReportFilter.MONTH
                }
                reportsViewModel.setReportFilter(filter)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun setupRecyclerViews() {
        categoryReportAdapter = CategoryReportAdapter()
        
        categoryLegendRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryReportAdapter
        }
        
        topCategoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = CategoryReportAdapter()
        }
    }
    
    private fun setupPieChart() {
        spendingPieChart.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 40f
            setHoleColor(android.graphics.Color.WHITE)
            transparentCircleRadius = 50f
            setDrawCenterText(true)
            centerText = "Spending"
            setCenterTextSize(16f)
            setCenterTextColor(requireContext().getColor(R.color.primary_navy))
            legend.isEnabled = false
        }
    }
    
    private fun setupObservers() {
        // Observe income, expenses, and balance
        reportsViewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            reportsIncomeAmount.text = CurrencyUtils.formatCurrency(income)
        }
        
        reportsViewModel.totalExpenses.observe(viewLifecycleOwner) { expenses ->
            reportsExpensesAmount.text = CurrencyUtils.formatCurrency(expenses)
        }
        
        reportsViewModel.currentBalance.observe(viewLifecycleOwner) { balance ->
            reportsBalanceAmount.text = CurrencyUtils.formatCurrency(balance)
        }
        
        // Observe spending by category
        reportsViewModel.spendingByCategory.observe(viewLifecycleOwner) { spendingData ->
            if (spendingData.isNullOrEmpty()) {
                spendingPieChart.visibility = View.GONE
                categoryLegendRecyclerView.visibility = View.GONE
                noSpendingDataTextView.visibility = View.VISIBLE
            } else {
                spendingPieChart.visibility = View.VISIBLE
                categoryLegendRecyclerView.visibility = View.VISIBLE
                noSpendingDataTextView.visibility = View.GONE
                
                updatePieChart(spendingData)
                categoryReportAdapter.submitList(spendingData)
            }
        }
        
        // Observe top categories
        reportsViewModel.topCategories.observe(viewLifecycleOwner) { topCategories ->
            val adapter = topCategoriesRecyclerView.adapter as CategoryReportAdapter
            adapter.submitList(topCategories)
        }
    }
    
    private fun updatePieChart(spendingData: List<com.example.easebudgetv1.viewmodel.CategorySpendingData>) {
        val entries = spendingData.map { data ->
            PieEntry(data.amount.toFloat(), data.categoryName)
        }
        
        val colorList = spendingData.map { data ->
            try {
                android.graphics.Color.parseColor(data.categoryColor)
            } catch (e: Exception) {
                requireContext().getColor(R.color.accent_teal)
            }
        }
        
        val dataSet = PieDataSet(entries, "Spending by Category").apply {
            this.colors = colorList
            valueTextSize = 12f
            valueTextColor = android.graphics.Color.WHITE
            valueFormatter = PercentFormatter(spendingPieChart)
        }
        
        val pieData = PieData(dataSet)
        spendingPieChart.data = pieData
        spendingPieChart.invalidate()
    }
}
