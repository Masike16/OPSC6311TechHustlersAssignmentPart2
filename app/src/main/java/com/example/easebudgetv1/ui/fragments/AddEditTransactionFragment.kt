package com.example.easebudgetv1.ui.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.easebudgetv1.R
import com.example.easebudgetv1.data.database.EaseBudgetDatabase
import com.example.easebudgetv1.data.database.entities.Transaction
import com.example.easebudgetv1.data.database.entities.TransactionType
import com.example.easebudgetv1.data.repository.EaseBudgetRepository
import com.example.easebudgetv1.ui.adapters.CategorySpinnerAdapter
import com.example.easebudgetv1.utils.*
import com.example.easebudgetv1.viewmodel.TransactionViewModel
import com.example.easebudgetv1.viewmodel.TransactionViewModelFactory
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.util.*

// (Author, 2024) Dialog fragment for adding and editing transactions
class AddEditTransactionFragment : DialogFragment() {
    
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var categoryAdapter: CategorySpinnerAdapter
    private lateinit var sessionManager: SessionManager
    
    // Views
    private lateinit var toolbar: com.google.android.material.appbar.MaterialToolbar
    private lateinit var expenseRadioButton: RadioButton
    private lateinit var incomeRadioButton: RadioButton
    private lateinit var amountEditText: TextInputEditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var categorySpinner: Spinner
    private lateinit var dateEditText: TextInputEditText
    private lateinit var receiptImageView: ImageView
    private lateinit var cameraButton: Button
    private lateinit var galleryButton: Button
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button
    
    // Data
    private var transactionId: Long = 0
    private var receiptPath: String? = null
    private var selectedDate: Long = System.currentTimeMillis()
    private var currentPhotoPath: String? = null
    
    // Permission launchers
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }
    
    private val galleryPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchGallery()
        } else {
            Toast.makeText(requireContext(), "Storage permission required", Toast.LENGTH_SHORT).show()
        }
    }
    
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoPath != null) {
            receiptPath = currentPhotoPath
            loadReceiptImage()
        }
    }
    
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val file = ImageUtils.createImageFile(requireContext())
            val bitmap = ImageUtils.getBitmapFromUri(requireContext(), it)
            if (bitmap != null && ImageUtils.saveBitmapToFile(bitmap, file)) {
                receiptPath = file.absolutePath
                loadReceiptImage()
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_EasEBudgetV1_NoActionBar)
        
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
        
        transactionViewModel = viewModels<TransactionViewModel> {
            TransactionViewModelFactory(repository, currentUserId)
        }.value
        
        // Get transaction ID from arguments (for editing)
        transactionId = arguments?.getLong("transactionId", 0) ?: 0
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_add_edit_transaction, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupToolbar()
        setupSpinner()
        setupObservers()
        setupClickListeners()
        
        if (transactionId > 0) {
            loadTransactionData()
        } else {
            setupDefaults()
        }
    }
    
    private fun initializeViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        expenseRadioButton = view.findViewById(R.id.expenseRadioButton)
        incomeRadioButton = view.findViewById(R.id.incomeRadioButton)
        amountEditText = view.findViewById(R.id.amountEditText)
        descriptionEditText = view.findViewById(R.id.descriptionEditText)
        categorySpinner = view.findViewById(R.id.categorySpinner)
        dateEditText = view.findViewById(R.id.dateEditText)
        receiptImageView = view.findViewById(R.id.receiptImageView)
        cameraButton = view.findViewById(R.id.cameraButton)
        galleryButton = view.findViewById(R.id.galleryButton)
        cancelButton = view.findViewById(R.id.cancelButton)
        saveButton = view.findViewById(R.id.saveButton)
    }
    
    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            dismiss()
        }
        
        if (transactionId > 0) {
            toolbar.title = "Edit Transaction"
        }
    }
    
    private fun setupSpinner() {
        categoryAdapter = CategorySpinnerAdapter(requireContext())
        categorySpinner.adapter = categoryAdapter
        
        // Observe categories
        transactionViewModel.categories.observe(viewLifecycleOwner, Observer { categories ->
            categoryAdapter.updateCategories(categories)
        })
    }
    
    private fun setupObservers() {
        transactionViewModel.transactionState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is com.example.easebudgetv1.viewmodel.TransactionState.Success -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                is com.example.easebudgetv1.viewmodel.TransactionState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        })
    }
    
    private fun setupClickListeners() {
        dateEditText.setOnClickListener {
            showDatePicker()
        }
        
        cameraButton.setOnClickListener {
            checkCameraPermission()
        }
        
        galleryButton.setOnClickListener {
            checkGalleryPermission()
        }
        
        cancelButton.setOnClickListener {
            dismiss()
        }
        
        saveButton.setOnClickListener {
            saveTransaction()
        }
    }
    
    private fun setupDefaults() {
        expenseRadioButton.isChecked = true
        dateEditText.setText(DateUtils.formatDate(selectedDate))
    }
    
    private fun loadTransactionData() {
        transactionViewModel.getTransactionById(transactionId).observe(viewLifecycleOwner, Observer { transaction ->
            transaction?.let {
                if (it.type == TransactionType.INCOME) {
                    incomeRadioButton.isChecked = true
                } else {
                    expenseRadioButton.isChecked = true
                }
                amountEditText.setText(it.amount.toString())
                descriptionEditText.setText(it.description)
                selectedDate = it.date
                dateEditText.setText(DateUtils.formatDate(selectedDate))
                receiptPath = it.receiptPath
                loadReceiptImage()
                
                // Select category in spinner
                transactionViewModel.categories.observe(viewLifecycleOwner, object : Observer<List<com.example.easebudgetv1.data.database.entities.Category>> {
                    override fun onChanged(value: List<com.example.easebudgetv1.data.database.entities.Category>) {
                        val position = value.indexOfFirst { cat -> cat.id == it.categoryId }
                        if (position >= 0) {
                            categorySpinner.setSelection(position)
                        }
                        transactionViewModel.categories.removeObserver(this)
                    }
                })
            }
        })
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDate
        
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = calendar.timeInMillis
                dateEditText.setText(DateUtils.formatDate(selectedDate))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        datePickerDialog.show()
    }
    
    private fun checkCameraPermission() {
        if (PermissionUtils.hasCameraPermissions(requireActivity())) {
            launchCamera()
        } else {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }
    
    private fun checkGalleryPermission() {
        if (PermissionUtils.hasStoragePermissions(requireActivity())) {
            launchGallery()
        } else {
            galleryPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
    
    private fun launchCamera() {
        val photoFile = ImageUtils.createImageFile(requireContext())
        currentPhotoPath = photoFile.absolutePath
        
        val photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        
        cameraLauncher.launch(photoUri)
    }
    
    private fun launchGallery() {
        galleryLauncher.launch("image/*")
    }
    
    private fun loadReceiptImage() {
        receiptPath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                receiptImageView.visibility = View.VISIBLE
                Glide.with(this)
                    .load(file)
                    .placeholder(R.drawable.ic_receipt)
                    .error(R.drawable.ic_receipt)
                    .into(receiptImageView)
            }
        }
    }
    
    private fun saveTransaction() {
        val amountStr = amountEditText.text?.toString()
        val description = descriptionEditText.text?.toString()?.trim()
        
        if (amountStr.isNullOrEmpty() || description.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        val amount = try { amountStr.toDouble() } catch(e: Exception) { 0.0 }
        if (amount <= 0.0) {
            Toast.makeText(requireContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }
        
        val type = if (incomeRadioButton.isChecked) TransactionType.INCOME else TransactionType.EXPENSE
        val categoryPosition = categorySpinner.selectedItemPosition
        val categoryId = if (categoryPosition >= 0) categoryAdapter.getItemId(categoryPosition) else null
        
        val transaction = Transaction(
            id = if (transactionId > 0) transactionId else 0,
            userId = sessionManager.getUserId(),
            categoryId = categoryId,
            amount = amount,
            date = selectedDate,
            description = description,
            receiptPath = receiptPath,
            type = type
        )
        
        if (transactionId > 0) {
            transactionViewModel.updateTransaction(transaction)
        } else {
            transactionViewModel.addTransaction(transaction)
        }
    }
}
