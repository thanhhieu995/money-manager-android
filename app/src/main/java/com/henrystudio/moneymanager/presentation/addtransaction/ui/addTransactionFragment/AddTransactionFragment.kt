package com.henrystudio.moneymanager.presentation.addtransaction.ui.addTransactionFragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.core.util.Helper.Companion.formatPickedDate
import com.henrystudio.moneymanager.core.util.Helper.Companion.getFormattedDateToday
import com.henrystudio.moneymanager.core.util.Helper.Companion.parseDisplayDateToLocalDate
import com.henrystudio.moneymanager.databinding.FragmentAddTransactionBinding
import com.henrystudio.moneymanager.core.util.Helper.Companion.showToastWithIcon
import com.henrystudio.moneymanager.data.model.CategoryType
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.addtransaction.ui.addItemFragment.AddItemFragment
import com.henrystudio.moneymanager.presentation.addtransaction.AddTransactionActivityViewModel
import com.henrystudio.moneymanager.presentation.addtransaction.ui.editItemFragment.EditItemDialogFragment
import com.henrystudio.moneymanager.presentation.addtransaction.components.adapter.AccountAdapter
import com.henrystudio.moneymanager.presentation.addtransaction.components.adapter.ExpandableCategoryAdapter
import com.henrystudio.moneymanager.presentation.addtransaction.components.viewholder.SharedTransactionHolder
import com.henrystudio.moneymanager.presentation.addtransaction.model.AddItemAction
import com.henrystudio.moneymanager.presentation.addtransaction.model.AddTransactionEvent
import com.henrystudio.moneymanager.presentation.addtransaction.model.CategoryItem
import com.henrystudio.moneymanager.presentation.addtransaction.model.SaveResult
import com.henrystudio.moneymanager.presentation.model.AddItemSource
import com.henrystudio.moneymanager.presentation.model.ItemType
import com.henrystudio.moneymanager.presentation.model.SaveTransactionParams
import com.henrystudio.moneymanager.presentation.model.TransactionType
import com.henrystudio.moneymanager.presentation.viewmodel.AccountViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.CategoryViewModel
import com.henrystudio.moneymanager.presentation.views.bottomNavigation.dailyNavigate.PrefsManager.saveLastDate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class AddTransactionFragment : Fragment() {
    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!
    private var isIncome = false
    private lateinit var dateTextView: TextView
    private lateinit var incomeButton: MaterialButton
    private lateinit var expenseButton: MaterialButton
    private lateinit var edtAmount: EditText
    private lateinit var edtCategory: EditText
    private lateinit var edtAccount: EditText
    private lateinit var edtNote: AutoCompleteTextView
    private lateinit var saveButton: Button
    private lateinit var continueButton: Button
    private lateinit var layoutSave: LinearLayout
    private lateinit var layoutEdit: LinearLayout
    private lateinit var deleteButton: Button
    private lateinit var copyButton: Button
    private lateinit var bookMarkButton: Button
    private lateinit var formattedDate: String
    private var transactionFromIntent: Transaction? = null
    private var isEditMode = false
    private val viewModel: AddTransactionFragmentViewModel by viewModels()
    private val activityViewModel: AddTransactionActivityViewModel by activityViewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()
    private val accountViewModel: AccountViewModel by viewModels()
    private var categoryJob: Job? = null
    private var accountJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        listOf(
            edtAmount,
            edtNote,
            edtAccount,
            edtCategory,
            dateTextView
        ).forEach { v ->
            v.setOnClickListener { switchToAddModeIfEditing() }
            if (v is EditText) {
                v.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) switchToAddModeIfEditing()
                }
            }
        }
        edtCategory.setOnTouchListener { _, _ ->
            switchToAddModeIfEditing()
            false
        }

        edtAccount.setOnTouchListener { _, _ ->
            switchToAddModeIfEditing()
            false
        }

        formattedDate = getFormattedDateToday()

        handleToAddTransaction()

        dateTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val newFormattedDate = formatPickedDate(selectedYear, selectedMonth, selectedDay)
                dateTextView.text = newFormattedDate
            }, year, month, day)

            datePicker.show()
        }

        incomeButton.setOnClickListener {
            onTransactionTypeChanged(true)
            activityViewModel.transactionTypeChanged(TransactionType.INCOME)
        }

        expenseButton.setOnClickListener {
            onTransactionTypeChanged(false)
            activityViewModel.transactionTypeChanged(TransactionType.EXPENSE)
        }

        categoryClick()
        amountTextChangeListener()
        categoryTextChangeListener()
        accountTextChangeListener()

        edtAmount.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_NEXT ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                event?.keyCode == KeyEvent.KEYCODE_ENTER
            ) {
                navigateNextEmptyField()
                true
            } else false
        }

        edtAccount.setOnClickListener {
            val tintColor = if (isIncome) R.color.income else R.color.red
            if (edtAccount.text.isEmpty()) {
                edtAccount.backgroundTintList = ContextCompat.getColorStateList(requireContext(), tintColor)
            }
            showAccountBottomDialog(
                requireContext().getString(R.string.account),
                edtAccount,
            )
        }

        saveButton.setOnClickListener {
            viewModel.saveTransaction(
               buildSaveParams(true)
            )
        }

        continueButton.setOnClickListener {
            viewModel.saveTransaction(buildSaveParams(false))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    state.noteSuggestions.let { suggestions ->
                        val adapterNote = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            suggestions
                        )
                        edtNote.setAdapter(adapterNote)
                    }
                    state.saveResult?.let { result ->
                        when (result) {
                            is SaveResult.Success -> {
                                viewModel.clearSaveResult()
                                SharedTransactionHolder.currentFilterDate = dateTextView.text.toString()
                                parseDisplayDateToLocalDate(dateTextView.text.toString())
                                    ?.let { saveLastDate(requireContext(), it) }
                                if (result.closeAfterSave) {
                                    SharedTransactionHolder.scrollToAddedTransaction = true
                                } else {
                                    isEditMode = false
                                    transactionFromIntent = null
                                    showToastWithIcon(
                                        requireContext(),
                                        requireContext().getString(R.string.saved)
                                    )
                                    edtAmount.setText("")
                                    edtCategory.setText("")
                                    edtAccount.setText("")
                                    edtNote.setText("")
                                    edtAmount.requestFocus()
                                }
                            }
                            is SaveResult.Error -> {
                                viewModel.clearSaveResult()
                                when (result.message) {
                                    "fill_required" -> showToastWithIcon(
                                        requireContext(),
                                        requireContext().getString(R.string.error_fill_category_account)
                                    )
                                    else -> showToastWithIcon(requireContext(), result.message)
                                }
                            }
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.event.collect { event ->
                when(event) {
                    AddTransactionEvent.NavigateBackToDaily -> {
                        navigateBackToDaily()
                    }

                    else -> {}
                }
            }
        }
    }

    private fun navigateBackToDaily() {
        requireActivity().finish()
    }

    private fun init() {
        dateTextView = binding.fragmentAddTransactionDate
        incomeButton = binding.fragmentAddTransactionBtnIncome
        expenseButton = binding.fragmentAddTransactionBtnExpense
        edtAmount = binding.fragmentAddTransactionAmount
        edtCategory = binding.fragmentAddTransactionEdtCategory
        edtAccount = binding.fragmentAddTransactionEdtAccount
        edtNote = binding.fragmentAddTransactionEdtNote
        saveButton = binding.fragmentAddTransactionBtnSave
        continueButton = binding.fragmentAddTransactionBtnContinue
        layoutSave = binding.fragmentAddTransactionLayoutSave
        layoutEdit = binding.fragmentAddTransactionLayoutEdit
        deleteButton = binding.fragmentAddTransactionBtnDelete
        copyButton = binding.fragmentAddTransactionBtnCopy
        bookMarkButton = binding.fragmentAddTransactionBtnBookmark
    }

    private fun showAccountBottomDialog(
        title: String,
        targetEditText: EditText,
    ) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_dialog_add, null)
        val recyclerView = view.findViewById<RecyclerView>(R.id.bottom_dialog_add_recyclerView)
        val titleBottom = view.findViewById<TextView>(R.id.bottom_dialog_add_title)
        val addButton = view.findViewById<ImageButton>(R.id.bottom_dialog_add_btn_add)
        val editButton = view.findViewById<ImageButton>(R.id.bottom_dialog_add_btn_edit)
        val closeButton = view.findViewById<ImageButton>(R.id.bottom_dialog_add_btn_close)
        titleBottom.text = title
        
        val adapter = AccountAdapter { selectedItem ->
            targetEditText.setText(selectedItem.name)
            bottomSheetDialog.dismiss()
            if (targetEditText.id == R.id.fragment_add_transaction_edtAccount) {
                navigateNextEmptyField()
            }
        }
        recyclerView.layoutManager = GridLayoutManager(context, 3)
        recyclerView.adapter = adapter

        accountJob?.cancel()
        accountJob = lifecycleScope.launch {
                accountViewModel.allAccounts.collectLatest { accounts ->
                    adapter.updateData(accounts)
                }
        }

        bottomSheetDialog.setOnDismissListener {
            accountJob?.cancel()
        }

        addButton.setOnClickListener {
            activityViewModel.onAddItemClicked(
                AddItemAction.FromAddTransaction,
                itemType = ItemType.ACCOUNT
            )

            bottomSheetDialog.dismiss()
        }

        editButton.setOnClickListener {
            activityViewModel.onAddItemClicked(
                AddItemAction.FromEditAccount(targetEditText.text.toString()),
                itemType = ItemType.ACCOUNT
            )

            bottomSheetDialog.dismiss()
        }

        closeButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }

    private fun switchToAddModeIfEditing() {
        if (isEditMode) {
            isEditMode = false
            transactionFromIntent = null
            layoutSave.visibility = View.VISIBLE
            layoutEdit.visibility = View.GONE
        }
    }

    private fun setTransactionType(isIncomeType: Boolean, isEdit: Boolean) {
        isIncome = isIncomeType

        incomeButton.isChecked = isIncomeType
        expenseButton.isChecked = !isIncomeType
    }

    private fun handleToAddTransaction() {
        transactionFromIntent = arguments?.getSerializable("transaction") as? Transaction

        if (transactionFromIntent == null) {
            showAddMode()
        } else {
            showEditMode(transactionFromIntent!!)
        }
    }

    private fun showAddMode() {
        setTransactionType(isIncome, false)
        if (!isEditMode) {
            edtAmount.post {
                edtAmount.requestFocusFromTouch()
                edtAmount.postDelayed({
                    focusWithKeyboard(edtAmount)
                }, 100)
            }
        }
        layoutSave.visibility = View.VISIBLE
        layoutEdit.visibility = View.GONE
        continueButton.visibility = View.VISIBLE
        if (!isEditMode) {
            dateTextView.text = formattedDate
        }
    }

    private fun showEditMode(transaction: Transaction) {
        isEditMode = true
        continueButton.visibility = View.GONE
        layoutSave.visibility = View.GONE
        layoutEdit.visibility = View.VISIBLE

        populateTransactionFields(transaction)
        setTransactionType(transaction.isIncome, true)

        copyButton.setOnClickListener {
            isEditMode = false
            layoutSave.visibility = View.VISIBLE
            layoutEdit.visibility = View.GONE
            populateTransactionFields(transaction)
            dateTextView.text = formattedDate
        }

        deleteButton.setOnClickListener {
            viewModel.deleteTransaction(transaction)
            Toast.makeText(
                context,
                requireContext().getString(R.string.transaction_delete),
                Toast.LENGTH_SHORT
            ).show()
            requireActivity().finish()
            requireActivity().overridePendingTransition(
                R.anim.no_animation,
                R.anim.slide_out_right
            )
        }

        bookMarkButton.setOnClickListener {
            val updated = transaction.copy(isBookmarked = true)
            viewModel.updateTransaction(updated)
            Toast.makeText(
                context,
                requireContext().getString(R.string.bookmarked),
                Toast.LENGTH_SHORT
            ).show()
            requireActivity().finish()
            requireActivity().overridePendingTransition(
                R.anim.no_animation,
                R.anim.slide_out_right
            )
        }
    }

    private fun populateTransactionFields(transaction: Transaction) {
        edtAmount.setText(transaction.amount.toString())
        edtCategory.setText(
            if (transaction.categorySubName.isNotEmpty()) {
                "${transaction.categoryParentName}/${transaction.categorySubName}"
            } else {
                transaction.categoryParentName
            }
        )
        edtAccount.setText(transaction.account)
        edtNote.setText(transaction.note)
        dateTextView.text = transaction.date
    }

    private fun categoryClick() {
        edtCategory.setOnClickListener {
            val selectedType = if (isIncome) CategoryType.INCOME else CategoryType.EXPENSE
            val tintColor = if (isIncome) R.color.income else R.color.red
            if (edtCategory.text.isEmpty()) {
                edtCategory.backgroundTintList = ContextCompat.getColorStateList(requireContext(), tintColor)
            }

            categoryJob?.cancel()
            categoryJob =  viewLifecycleOwner.lifecycleScope.launch {
                val list = categoryViewModel
                    .getCategoriesByType(selectedType)
                    .first{it.isNotEmpty()}

                val treeItems = Helper.buildCategoryTree(list)

                showCategoryBottomDialog(
                    requireContext().getString(R.string.category),
                    treeItems,
                    edtCategory,
                )
            }
        }
    }

    private fun amountTextChangeListener() {
        edtAmount.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return

                isFormatting = true

                val cleanString = s.toString().replace("[^\\d]".toRegex(), "")

                if (cleanString.isNotEmpty()) {
                    val number = cleanString.toLongOrNull() ?: 0L
                    val formatted = Helper.formatCurrency(number.toDouble())

                    edtAmount.setText(formatted)
                    edtAmount.setSelection(formatted.length - 1) // giữ cursor trước "đ"
                } else {
                    edtAmount.setText("")
                }

                isFormatting = false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
    private fun categoryTextChangeListener() {}
    private fun accountTextChangeListener() {}

    private fun openAddItemFragment(type: ItemType, categoryType: CategoryType) {
        val fragment = AddItemFragment().apply {
            arguments = Bundle().apply {
                putSerializable("item_type", type)
                putSerializable("category_type", categoryType)
                putSerializable("source", AddItemSource.FROM_ADD_TRANSACTION)
            }
        }

        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.no_animation,
                R.anim.no_animation,
                R.anim.slide_out_right
            )
            .replace(R.id.fragment_container_add_transaction, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun openEditAccountFragment(type: ItemType, categoryType: CategoryType) {
        val fragment = EditItemDialogFragment().apply {
            arguments = Bundle().apply {
                putSerializable("source", AddItemSource.FROM_ADD_TRANSACTION)
            }
        }

        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.no_animation,
                R.anim.no_animation,
                R.anim.slide_out_right
            )
            .replace(R.id.fragment_container_add_transaction, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun buildSaveParams(closeAfterSave: Boolean): SaveTransactionParams {
        return SaveTransactionParams(
            amount = edtAmount.text.toString()
                .replace("[^\\d]".toRegex(), "")
                .toLongOrNull()
                ?.toString() ?: "0",
            category = edtCategory.text.toString(),
            account = edtAccount.text.toString(),
            note = edtNote.text.toString(),
            date = dateTextView.text.toString(),
            isIncome = isIncome,
            existing = transactionFromIntent,
            closeAfterSave = closeAfterSave
        )
    }

    private fun showCategoryBottomDialog(
        title: String,
        categoryItems: List<CategoryItem>,
        targetEditText: EditText,
    ) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_dialog_add, null)
        val recyclerView = view.findViewById<RecyclerView>(R.id.bottom_dialog_add_recyclerView)
        val titleBottom = view.findViewById<TextView>(R.id.bottom_dialog_add_title)
        val addButton = view.findViewById<ImageButton>(R.id.bottom_dialog_add_btn_add)
        val editButton = view.findViewById<ImageButton>(R.id.bottom_dialog_add_btn_edit)
        val closeButton = view.findViewById<ImageButton>(R.id.bottom_dialog_add_btn_close)
        var selectedCategory: CategoryItem? = null
        titleBottom.text = title
        val adapter = ExpandableCategoryAdapter(categoryItems) { selectedItem ->
            selectedCategory = selectedItem
            val parentEmoji = selectedItem.parentEmoji ?: ""
            val parentName =
                if (selectedItem.parentName == null) "" else selectedItem.parentName + "/"
            targetEditText.setText("$parentEmoji $parentName ${selectedItem.emoji} ${selectedItem.name}")
            bottomSheetDialog.dismiss()
            // Finish choose category and show account, finish account and show note auto
            if (targetEditText.id == R.id.fragment_add_transaction_edtCategory) {
                navigateNextEmptyField()
            }
        }

        val layoutManager = GridLayoutManager(context, 3)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.getItemViewType(position)) {
                    0 -> 1 // cha
                    1 -> 3 // nhóm con
                    else -> 1
                }
            }
        }
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        addButton.setOnClickListener {
            activityViewModel.onAddItemClicked(
                AddItemAction.FromAddTransaction,
                ItemType.CATEGORY
            )
            bottomSheetDialog.dismiss()
        }

        editButton.setOnClickListener {
            selectedCategory?.let {
                activityViewModel.onEditItemClicked(
                    AddItemAction.FromEditCategory(it),
                    ItemType.CATEGORY
                )
            }

            bottomSheetDialog.dismiss()
        }

        closeButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }

    private fun onTransactionTypeChanged(newType: Boolean) {
        if (isIncome == newType) return

        setTransactionType(newType, false)

        edtCategory.setText("")
        edtAccount.setText("")

        navigateNextEmptyField()
    }

    private fun navigateNextEmptyField() {
        when {
            edtAmount.text.isNullOrBlank() -> {
                focusWithKeyboard(edtAmount)
            }

            edtCategory.text.isNullOrBlank() -> {
                hideKeyboard()
               edtCategory.postDelayed({
                   edtCategory.performClick()
               }, 100)
            }

            edtAccount.text.isNullOrBlank() -> {
                hideKeyboard()
                edtAccount.postDelayed({
                    edtAccount.performClick()
                }, 100)
            }

            edtNote.text.isNullOrBlank() -> {
                focusWithKeyboard(edtNote)
            }

            else -> {
                // 👉 tất cả đã có data → không làm gì
            }
        }
    }

    private fun focusWithKeyboard(view: View) {
        view.post {
            view.requestFocusFromTouch()

            view.postDelayed({
                val imm = requireContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            }, 100)
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}
