package com.henrystudio.moneymanager.presentation.views.addtransaction

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.core.util.Helper
import com.henrystudio.moneymanager.core.util.Helper.Companion.formatPickedDate
import com.henrystudio.moneymanager.core.util.Helper.Companion.getFormattedDateToday
import com.henrystudio.moneymanager.core.util.Helper.Companion.parseDisplayDateToLocalDate
import com.henrystudio.moneymanager.databinding.FragmentAddTransactionBinding
import com.henrystudio.moneymanager.core.util.Helper.Companion.showToastWithIcon
import com.henrystudio.moneymanager.data.model.Account
import com.henrystudio.moneymanager.data.model.CategoryType
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.model.AddItemSource
import com.henrystudio.moneymanager.presentation.model.ItemType
import com.henrystudio.moneymanager.presentation.model.SaveTransactionParams
import com.henrystudio.moneymanager.presentation.viewmodel.AccountViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.CategoryViewModel
import com.henrystudio.moneymanager.presentation.views.bottomNavigation.dailyNavigate.PrefsManager.saveLastDate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class AddTransactionFragment : Fragment() {
    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!

    private var isIncome = false
    private lateinit var dateTextView: TextView
    private lateinit var incomeButton: com.google.android.material.button.MaterialButton
    private lateinit var expenseButton: com.google.android.material.button.MaterialButton
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
    private val viewModel: AddTransactionViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()
    private val accountViewModel: AccountViewModel by viewModels()

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
            setTransactionType(true, false)
            Handler(Looper.getMainLooper()).postDelayed({
                edtCategory.setText("")
                edtCategory.performClick()
            }, 200)
        }

        expenseButton.setOnClickListener {
            setTransactionType(false, false)
            Handler(Looper.getMainLooper()).postDelayed({
                edtCategory.setText("")
                edtCategory.performClick()
            }, 200)
        }

        categoryClick()
        amountTextChangeListener()
        categoryTextChangeListener()
        accountTextChangeListener()

        edtAccount.setOnClickListener {
            val selectedType = if (isIncome) CategoryType.INCOME else CategoryType.EXPENSE
            val tintColor = if (isIncome) R.color.income else R.color.red
            if (edtAccount.text.isEmpty()) {
                edtAccount.backgroundTintList = ContextCompat.getColorStateList(requireContext(), tintColor)
            }
            viewLifecycleOwner.lifecycleScope.launch {
                val accountList = accountViewModel.allAccounts.first()

                showAccountBottomDialog(
                    requireContext().getString(R.string.account),
                    accountList,
                    edtAccount,
                    onAddClick = { openAddItemFragment(ItemType.ACCOUNT, selectedType) },
                    onEditClick = { openEditAccountFragment(ItemType.ACCOUNT, selectedType) }
                )
            }
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
                                    (saveButton.context as? AddTransactionActivity)?.onTransactionSaved()
                                } else {
                                    isEditMode = false
                                    transactionFromIntent = null
                                    showToastWithIcon(requireContext(), requireContext().getString(R.string.saved))
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
        accountList: List<Account>,
        targetEditText: EditText,
        onAddClick: () -> Unit,
        onEditClick: () -> Unit
    ) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_dialog_add, null)
        val recyclerView = view.findViewById<RecyclerView>(R.id.bottom_dialog_add_recyclerView)
        val titleBottom = view.findViewById<TextView>(R.id.bottom_dialog_add_title)
        val addButton = view.findViewById<ImageButton>(R.id.bottom_dialog_add_btn_add)
        val editButton = view.findViewById<ImageButton>(R.id.bottom_dialog_add_btn_edit)
        val closeButton = view.findViewById<ImageButton>(R.id.bottom_dialog_add_btn_close)
        titleBottom.text = title
        val adapter = AccountAdapter(accountList) { selectedItem ->
            targetEditText.setText(selectedItem.name)
            if (targetEditText.id == R.id.fragment_add_transaction_edtAccount) {
                edtNote.postDelayed({
                    focusNextField()
                }, 100)
            }
            bottomSheetDialog.dismiss()
        }
        recyclerView.layoutManager = GridLayoutManager(context, 3)
        recyclerView.adapter = adapter

        addButton.setOnClickListener {
            onAddClick()
            bottomSheetDialog.dismiss()
        }

        editButton.setOnClickListener {
            onEditClick()
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
        Handler(Looper.getMainLooper()).postDelayed({
            setTransactionType(isIncome, false)
            if (!isEditMode) {
                edtAmount.requestFocus()
            }
        }, 200)
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

            viewLifecycleOwner.lifecycleScope.launch {
                val list = categoryViewModel
                    .getCategoriesByType(selectedType)
                    .first()

                val treeItems = Helper.buildCategoryTree(list)

                showCategoryBottomDialog(
                    requireContext().getString(R.string.category),
                    treeItems,
                    edtCategory,
                    onAddClick = { openAddItemFragment(ItemType.CATEGORY, selectedType) },
                    onEditClick = { openEditAccountFragment(ItemType.CATEGORY, selectedType) }
                )
            }
        }
    }
    
    private fun amountTextChangeListener() {}
    private fun categoryTextChangeListener() {}
    private fun accountTextChangeListener() {}

    private fun focusNextField() {
        when {
            edtAmount.text.isNullOrBlank() -> {
                edtAmount.requestFocus()
            }
            edtCategory.text.isNullOrBlank() -> {
                edtCategory.requestFocus()
                edtCategory.performClick()
            }
            edtAccount.text.isNullOrBlank() -> {
                edtAccount.requestFocus()
                edtAccount.performClick()
            }
            edtNote.text.isNullOrBlank() -> {
                edtNote.requestFocus()
            }
        }
    }

    private fun openAddItemFragment(type: ItemType, categoryType: CategoryType) {
        val activity = requireActivity() as AddTransactionActivity
        val titleView = activity.titleCurrent
        activity.animateTitleToLeftOfIcon(titleView)
        activity.bookmarkIcon.visibility = View.GONE
        activity.addIcon.visibility = View.GONE
        activity.titleStack.addLast(titleView.text.toString())
        val titleIncoming = activity.titleIncoming
        activity.animateIncomingTitleToCenter(
            titleIncoming,
            requireContext().getString(R.string.add)
        )

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
        val activity = requireActivity() as AddTransactionActivity
        val titleView = activity.titleCurrent
        activity.animateTitleToLeftOfIcon(titleView)
        activity.updateTitleIncoming(
            if (type == ItemType.CATEGORY)
                requireContext().getString(R.string.category)
            else
                requireContext().getString(R.string.account)
        )
        val extraEditText = activity.titleIncoming
        activity.animateIncomingTitleToCenter(extraEditText, extraEditText.text.toString())
        activity.switchToAddIconWithFade()
        activity.titleStack.addLast(titleView.text.toString())
        activity.apply {
            currentItemType = type
            currentCategoryType = categoryType
        }

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
            amount = edtAmount.text.toString(),
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
        onEditClick: () -> Unit,
        onAddClick: () -> Unit
    ) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_dialog_add, null)
        val recyclerView = view.findViewById<RecyclerView>(R.id.bottom_dialog_add_recyclerView)
        val titleBottom = view.findViewById<TextView>(R.id.bottom_dialog_add_title)
        val addButton = view.findViewById<ImageButton>(R.id.bottom_dialog_add_btn_add)
        val editButton = view.findViewById<ImageButton>(R.id.bottom_dialog_add_btn_edit)
        val closeButton = view.findViewById<ImageButton>(R.id.bottom_dialog_add_btn_close)
        titleBottom.text = title
        val adapter = ExpandableCategoryAdapter(categoryItems) { selectedItem ->
            val parentEmoji = selectedItem.parentEmoji ?: ""
            val parentName =
                if (selectedItem.parentName == null) "" else selectedItem.parentName + "/"
            targetEditText.setText("$parentEmoji $parentName ${selectedItem.emoji} ${selectedItem.name}")

            // Finish choose category and show account, finish account and show note auto
            if (targetEditText.id == R.id.fragment_add_transaction_edtCategory) {
                Handler(Looper.getMainLooper()).postDelayed({
                    focusNextField()
                }, 100)
            }
            bottomSheetDialog.dismiss()
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
            onAddClick()
            bottomSheetDialog.dismiss()
        }

        editButton.setOnClickListener {
            onEditClick()
            bottomSheetDialog.dismiss()
        }

        closeButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }
}
