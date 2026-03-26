package com.henrystudio.moneymanager.presentation.addtransaction.ui.addTransactionFragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.res.ColorStateList
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
import com.henrystudio.moneymanager.core.util.Helper.Companion.setTextIfDifferent
import com.henrystudio.moneymanager.databinding.FragmentAddTransactionBinding
import com.henrystudio.moneymanager.core.util.Helper.Companion.showToastWithIcon
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.addtransaction.AddTransactionActivityViewModel
import com.henrystudio.moneymanager.presentation.addtransaction.components.adapter.AccountAdapter
import com.henrystudio.moneymanager.presentation.addtransaction.components.adapter.ExpandableCategoryAdapter
import com.henrystudio.moneymanager.presentation.addtransaction.components.viewholder.SharedTransactionHolder
import com.henrystudio.moneymanager.presentation.addtransaction.model.AddItemAction
import com.henrystudio.moneymanager.presentation.addtransaction.model.AddTransactionEvent
import com.henrystudio.moneymanager.presentation.addtransaction.model.CategoryItem
import com.henrystudio.moneymanager.presentation.addtransaction.model.FieldState
import com.henrystudio.moneymanager.presentation.addtransaction.model.FieldType
import com.henrystudio.moneymanager.presentation.addtransaction.model.SaveResult
import com.henrystudio.moneymanager.presentation.model.ItemType
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
    private val viewModel: AddTransactionFragmentViewModel by viewModels()
    private val activityViewModel: AddTransactionActivityViewModel by activityViewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()
    private val accountViewModel: AccountViewModel by viewModels()
    private var categoryJob: Job? = null
    private var accountJob: Job? = null
    private lateinit var defaultTintMap: Map<EditText, ColorStateList?>

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
        initDefaultTints()

        listOf(
            edtAmount,
            edtNote,
            edtAccount,
            edtCategory
        ).forEach { v ->
            v.setOnClickListener {
                viewModel.onUserStartEditing()
            }

            v.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    viewModel.onUserStartEditing()
                }
            }
        }
        edtAmount.setOnClickListener {
            viewModel.onAmountTouched()
        }

        edtCategory.setOnTouchListener { _, _ ->
            viewModel.onUserStartEditing()
            false
        }

        edtAccount.setOnTouchListener { _, _ ->
            viewModel.onUserStartEditing()
            false
        }

        formattedDate = getFormattedDateToday()

        handleToAddTransaction()

        dateTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker =
                DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                    val newFormattedDate =
                        formatPickedDate(selectedYear, selectedMonth, selectedDay)
                    viewModel.onDateChanged(newFormattedDate)
                }, year, month, day)

            datePicker.show()
        }

        incomeButton.setOnClickListener {
            viewModel.onTransactionTypeChanged(true)
            activityViewModel.transactionTypeChanged(TransactionType.INCOME)
        }

        expenseButton.setOnClickListener {
            viewModel.onTransactionTypeChanged(false)
            activityViewModel.transactionTypeChanged(TransactionType.EXPENSE)
        }

        categoryClick()
        amountTextChangeListener()
        categoryTextChangeListener()
        accountTextChangeListener()
        noteTextChangeListener()

        edtAmount.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_NEXT ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                event?.keyCode == KeyEvent.KEYCODE_ENTER
            ) {
                navigateNextEmptyField()
                true
            } else false
        }

        edtAmount.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                viewModel.onAmountTouched() // 🔥 thêm dòng này
            }
        }

        edtAccount.setOnClickListener {
            showAccountBottomDialog(
                requireContext().getString(R.string.account),
                edtAccount,
            )
        }

        saveButton.setOnClickListener {
            viewModel.onSaveClicked(true)
        }

        continueButton.setOnClickListener {
            viewModel.onSaveClicked(false)
        }

        copyButton.setOnClickListener {
            viewModel.onCopyClicked()
        }

        deleteButton.setOnClickListener {
            viewModel.onDeleteClicked()
        }

        bookMarkButton.setOnClickListener {
            viewModel.onBookmarkClicked()
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
                    state.amountFormatted.let {
                        edtAmount.setTextIfDifferent(it)
                        edtAmount.backgroundTintList = when (state.amountRaw.state) {
                            FieldState.IDLE -> defaultTintMap[edtAmount]
                            FieldState.ERROR -> ContextCompat.getColorStateList(requireContext(), R.color.red)
                            FieldState.VALID -> ContextCompat.getColorStateList(requireContext(), R.color.income)
                        }
                    }
                    state.category.let { category ->
                        edtCategory.setTextIfDifferent(category)
                    }
                    state.account.let { account ->
                        edtAccount.setTextIfDifferent(account)
                    }
                    state.note.let { note ->
                        edtNote.setTextIfDifferent(note)
                    }
                    state.isIncome.let { isIncome ->
                        incomeButton.isChecked = isIncome
                        expenseButton.isChecked = !isIncome
                    }
                    state.date.let { date ->
                        dateTextView.text = date
                    }
                    state.isEditMode.let { isEdit ->
                        layoutSave.visibility = if (isEdit) View.GONE else View.VISIBLE
                        layoutEdit.visibility = if (isEdit) View.VISIBLE else View.GONE
                    }
                    state.isContinueVisible.let { isVisible ->
                        continueButton.visibility = if (isVisible) View.VISIBLE else View.GONE
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.event.collect { event ->
                when (event) {
                    is AddTransactionEvent.NavigateBackToDaily -> {
                        navigateBackToDaily()
                    }

                    is AddTransactionEvent.FocusField -> {
                        when (event.fieldType) {
                            FieldType.AMOUNT -> {
                                focusWithKeyboard(edtAmount)
                            }
                            FieldType.CATEGORY -> {
                                hideKeyboard()
                                edtCategory.postDelayed({
                                    edtCategory.performClick()
                                }, 100)
                            }
                            FieldType.ACCOUNT -> {
                                hideKeyboard()
                                edtAccount.postDelayed({
                                    edtAccount.performClick()
                                }, 100)
                            }
                            FieldType.NOTE -> {
                                focusWithKeyboard(edtNote)
                            }
                            else -> {}
                        }
                    }
                    is AddTransactionEvent.CloseScreen -> {
                        SharedTransactionHolder.scrollToAddedTransaction = true
                    }
                    is AddTransactionEvent.ResetForm -> {
                        viewModel.resetForm()
                    }
                    is AddTransactionEvent.ShowToast -> {
                        val message = when (event.message) {
                            "transaction_delete" -> getString(R.string.transaction_delete)
                            "bookmarked" -> getString(R.string.bookmarked)
                            else -> event.message
                        }

                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                    is AddTransactionEvent.NavigateBack -> {
                        requireActivity().finish()
                        requireActivity().overridePendingTransition(
                            R.anim.no_animation,
                            R.anim.slide_out_right
                        )
                    }
                    is AddTransactionEvent.SaveCompleted -> {
                        SharedTransactionHolder.currentFilterDate = event.date
                        event.localDate?.let {
                            saveLastDate(requireContext(), it)
                        }
                        if (event.closeAfterSave) {
                            SharedTransactionHolder.scrollToAddedTransaction = true
                            requireActivity().finish()
                        } else {
                            viewModel.resetForm()
                        }
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

    private fun handleToAddTransaction() {
        val transaction = arguments?.getSerializable("transaction") as? Transaction
        viewModel.initTransaction(transaction)
    }

    private fun categoryClick() {
        edtCategory.setOnClickListener {
            val selectedType = viewModel.getSelectedCategoryType()

            categoryJob?.cancel()
            categoryJob = viewLifecycleOwner.lifecycleScope.launch {
                val list = categoryViewModel
                    .getCategoriesByType(selectedType)
                    .first { it.isNotEmpty() }

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
            override fun afterTextChanged(s: Editable?) {
                viewModel.onAmountChanged(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun categoryTextChangeListener() {
        edtCategory.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.onCategoryChanged(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun accountTextChangeListener() {
        edtAccount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.onAccountChanged(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun noteTextChangeListener() {
        edtNote.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                p0: CharSequence?,
                p1: Int,
                p2: Int,
                p3: Int
            ) {}

            override fun onTextChanged(
                p0: CharSequence?,
                p1: Int,
                p2: Int,
                p3: Int
            ) {}

            override fun afterTextChanged(s: Editable?) {
                viewModel.onNoteChanged(s.toString())
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
            targetEditText.setText(viewModel.formatCategoryDisplay(selectedItem))
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

    private fun navigateNextEmptyField() {
        viewModel.onNextClicked()
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

    private fun initDefaultTints() {
        defaultTintMap = mapOf(
            edtAmount to edtAmount.backgroundTintList,
            edtCategory to edtCategory.backgroundTintList,
            edtAccount to edtAccount.backgroundTintList,
            edtNote to edtNote.backgroundTintList
        )
    }
}
