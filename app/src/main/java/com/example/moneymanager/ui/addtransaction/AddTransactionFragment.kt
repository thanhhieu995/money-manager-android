package com.example.moneymanager.ui.addtransaction

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanager.R
import com.example.moneymanager.model.Transaction
import com.example.moneymanager.databinding.FragmentAddTransactionBinding
import com.example.moneymanager.helper.Helper
import com.example.moneymanager.helper.Helper.Companion.buildCategoryTree
import com.example.moneymanager.model.Account
import com.example.moneymanager.model.AppDatabase
import com.example.moneymanager.model.CategoryType
import com.example.moneymanager.ui.main.MainActivity
import com.example.moneymanager.viewmodel.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionFragment : Fragment() {

    private var _binding: FragmentAddTransactionBinding?= null
    private val binding get() = _binding!!

    private lateinit var viewModel: TransactionViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var accountViewModel: AccountViewModel
    private var isIncome: Boolean = false
    private var transactions: List<Transaction> = listOf()
    private lateinit var dateTextView: TextView
    private lateinit var incomeButton: Button
    private lateinit var expenseButton: Button
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
    private lateinit var iconBookmark: ImageView
    private lateinit var formattedDate: String
    private var transactionFromIntent: Transaction? = null

    private var shouldAnimateExit = false
    private var isEditMode = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

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
        ).forEach { view ->
            view.setOnClickListener { switchToAddModeIfEditing() }
            if (view is EditText) {
                view.setOnFocusChangeListener { _, hasFocus ->
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

        // Lấy ngày hiện tại và hiển thị
        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd/MM/yy (EEE)", Locale.getDefault())
        formattedDate = dateFormat.format(currentDate)

        val accounts = listOf(
            CategoryItem(0,"💵", "Cash", "", "", false, emptyList(),false),
            CategoryItem(1,"🏦", "Bank Account", "", "", false, emptyList(),false),
            CategoryItem(2, "💳", "Credit Card", "", "", false, emptyList(),false),
            CategoryItem(3,"📱", "E-Wallet", "", "", false, emptyList(),false),
            CategoryItem(4,"🪙", "Crypto", "", "", false, emptyList(),false),
            CategoryItem(5,"📦", "Savings", "", "", false, emptyList(),false)
        )

        val dao = AppDatabase.getDatabase(requireActivity().application).transactionDao()
        val factory = TransactionViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]

        val daoCategory = AppDatabase.getDatabase(requireActivity().application).categoryDao()
        val factoryCategory = CategoryViewModelFactory(daoCategory)
        categoryViewModel = ViewModelProvider(this, factoryCategory)[CategoryViewModel::class.java]

        val daoAccount = AppDatabase.getDatabase(requireActivity().application).accountDao()
        val factoryAccount = AccountViewModelFactory(daoAccount)
        accountViewModel = ViewModelProvider(this, factoryAccount)[AccountViewModel::class.java]

        handleToAddTransaction()

        dateTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val pickedCalendar = Calendar.getInstance()
                pickedCalendar.set(selectedYear, selectedMonth, selectedDay)
                val newFormattedDate = dateFormat.format(pickedCalendar.time)
                dateTextView.text = newFormattedDate
            }, year, month, day)

            datePicker.show()
        }

        incomeButton.setOnClickListener {
            setTransactionType(
                isIncomeType = true,
                isEdit = false
            )
            Handler(Looper.getMainLooper()).postDelayed({
                edtCategory.setText("")
                edtCategory.performClick()
            }, 200)
        }

        expenseButton.setOnClickListener {
            setTransactionType(
                isIncomeType = false,
                isEdit = false
            )
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
            val tintColor = if (isIncome) R.color.income else R.color.red
            edtAccount.backgroundTintList = ContextCompat.getColorStateList(requireContext(), tintColor)
            accountViewModel.getAllAccount().observe(viewLifecycleOwner){ accountList ->
                showAccountBottomDialog("Account", accountList, edtAccount) {

                    val fragment = EditItemDialogFragment()

                    parentFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right,  // enter
                            R.anim.no_animation,    // exit
                            R.anim.no_animation,    // popEnter (khi quay lại)
                            R.anim.slide_out_right  // popExit (khi quay lại)
                        )
                        .replace(R.id.fragment_container_add_transaction, fragment) // thay fragment container ID
                        .addToBackStack(null)
                        .commit()
                }
            }
        }

        saveButton.setOnClickListener {
            saveTransaction {
                // Nếu đủ thông tin thì chuyển sang MainActivity
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
                requireActivity().finish() // kết thúc activity hiện tại
            }
        }

        continueButton.setOnClickListener {
            saveTransaction {
                Toast.makeText(context, "Giao dịch đã được lưu!", Toast.LENGTH_SHORT).show()
                // Reset các trường
                edtAmount.setText("")
                edtCategory.setText("")
                edtAccount.setText("")
                edtNote.setText("")
                edtAmount.requestFocus()
            }
        }

        // xu ly goi y khi input vao note
        viewModel.allTransactions.observe(viewLifecycleOwner) {
            transactions = it
            val contents = transactions.map { it.note }.distinct()
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                contents
            )
            edtNote.setAdapter(adapter)
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

    private fun saveTransaction(onSuccess: () -> Unit) {
        if (edtAmount.text.isEmpty() || edtCategory.text.isEmpty() || edtAccount.text.isEmpty()) {
            Toast.makeText(
                context,
                "Vui lòng nhập đầy đủ Amount, Category và Account",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val amount = edtAmount.text.toString()
            .replace("[^\\d]".toRegex(), "")
            .toDoubleOrNull() ?: 0.0

        if(isEditMode) {
            transactionFromIntent?.let { original ->
                val updatedTransaction = original.copy(
                    title = "",
                    category = edtCategory.text.toString(),
                    note = edtNote.text.toString().trim(),
                    account = edtAccount.text.toString(),
                    amount = amount,
                    isIncome = isIncome,
                    date = dateTextView.text.toString()
                )
                viewModel.update(updatedTransaction)
            }
        } else {
            val transaction = Transaction(
                title = "",
                category = edtCategory.text.toString(),
                note = edtNote.text.toString().trim(),
                account = edtAccount.text.toString(),
                amount = amount,
                isIncome = isIncome,
                date = dateTextView.text.toString()
            )
            viewModel.insert(transaction)
        }
        isEditMode = false
        onSuccess()
    }

    private fun showAccountBottomDialog(
        title: String,
        accountList: List<Account>,
        targetEditText: EditText,
        onEditClick: () -> Unit
    ) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_dialog_add, null)
        val recyclerView = view.findViewById<RecyclerView>(R.id.bottom_dialog_add_recyclerView)
        val titleBottom = view.findViewById<TextView>(R.id.bottom_dialog_add_title)
        val editButton = view.findViewById<ImageButton>(R.id.bottom_dialog_add_btn_edit)
        val closeButton = view.findViewById<ImageButton>(R.id.bottom_dialog_add_btn_close)
        titleBottom.text = title
        val adapter = AccountAdapter(accountList) { selectedItem ->
            targetEditText.setText(selectedItem.name)
            if (targetEditText.id == R.id.fragment_add_transaction_edtAccount) {
                edtNote.postDelayed({
                    edtNote.requestFocus()
                    showKeyboard(edtNote)
                }, 100)
            }
            bottomSheetDialog.dismiss()
        }
        val layoutManager = GridLayoutManager(context, 3)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

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

    @SuppressLint("MissingInflatedId")
    private fun showCategoryBottomDialog(
        title: String,
        categoryItems: List<CategoryItem>,
        targetEditText: EditText,
        onEditClick: () -> Unit
    ) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_dialog_add, null)
        val recyclerView = view.findViewById<RecyclerView>(R.id.bottom_dialog_add_recyclerView)
        val titleBottom = view.findViewById<TextView>(R.id.bottom_dialog_add_title)
        val editButton = view.findViewById<ImageButton>(R.id.bottom_dialog_add_btn_edit)
        val closeButton = view.findViewById<ImageButton>(R.id.bottom_dialog_add_btn_close)
        titleBottom.text = title
        val adapter = ExpandableCategoryAdapter(categoryItems) { selectedItem ->
            val parentEmoji = selectedItem.parentEmoji ?: ""
            val parentName = if (selectedItem.parentName == null) "" else selectedItem.parentName + "/"
            targetEditText.setText("$parentEmoji $parentName ${selectedItem.emoji} ${selectedItem.name}")

            // Finish choose category and show account, finish account and show note auto
            if (targetEditText.id == R.id.fragment_add_transaction_edtCategory) {
                edtAccount.requestFocus()
                edtAccount.performClick() // mở bottom sheet account
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

    private fun setTransactionType(
        isIncomeType: Boolean,
        isEdit: Boolean
    ) {
        isIncome = isIncomeType

        val activeColor = if (isIncomeType) Color.BLUE else Color.RED
        val inactiveColor = Color.WHITE
        val activeTextColor = Color.WHITE
        val inactiveTextColor = Color.BLACK

        incomeButton.setBackgroundColor(if (isIncomeType) activeColor else inactiveColor)
        incomeButton.setTextColor(if (isIncomeType) activeTextColor else inactiveTextColor)

        expenseButton.setBackgroundColor(if (isIncomeType) inactiveColor else activeColor)
        expenseButton.setTextColor(if (isIncomeType) inactiveTextColor else activeTextColor)

        saveButton.setBackgroundColor(activeColor)
        saveButton.setTextColor(activeTextColor)

        val titleTransaction = if (isIncomeType) "Income" else "Expense"
        (requireActivity() as AddTransactionActivity).updateTransactionTitle(titleTransaction)

        layoutSave.visibility = if (isEdit) View.GONE else View.VISIBLE
        layoutEdit.visibility = if (isEdit) View.VISIBLE else View.GONE
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
            // set ui when show add mode
            setTransactionType(isIncome, false)
            // show keyboard amount and show choose category auto
            edtAmount.requestFocus()
            showKeyboard(edtAmount)
            edtAmount.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                    edtCategory.requestFocus()
                    edtCategory.performClick()
                    true
                } else false
            }
        }, 200)
        // hide and show layout save or edit
        layoutSave.visibility = View.VISIBLE
        layoutEdit.visibility = View.GONE
        continueButton.visibility = View.VISIBLE
        if (!isEditMode){
            dateTextView.text = formattedDate
        }
    }

    private fun showKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
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
            AlertDialog.Builder(requireContext())
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Yes") { dialog, _ ->
                    viewModel.delete(transaction)
                    Toast.makeText(context, "Transaction deleted", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    requireActivity().finish()
                    requireActivity().overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right)
                }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        bookMarkButton.setOnClickListener {
            val updated = transaction.copy(isBookmarked = true)
            viewModel.update(updated)
            Toast.makeText(context, "Bookmarked!", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
            requireActivity().overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right)
        }
    }

    private fun populateTransactionFields(transaction: Transaction) {
        edtAmount.setText(Helper.formatCurrency(transaction.amount))
        edtCategory.setText(transaction.category)
        edtAccount.setText(transaction.account)
        edtNote.setText(transaction.note)
        dateTextView.text = transaction.date
    }

    private fun categoryClick() {
        edtCategory.setOnClickListener {
            val selectedType = if (isIncome) CategoryType.INCOME else CategoryType.EXPENSE
            val tintColor = if (isIncome) R.color.income else R.color.red
            edtCategory.backgroundTintList = ContextCompat.getColorStateList(requireContext(), tintColor)

            categoryViewModel.getCategoriesByType(selectedType).observe(viewLifecycleOwner) { list ->
                val treeItems = buildCategoryTree(list)
                // Gắn thêm "Add Category" ở cuối nếu muốn
                val addItem = CategoryItem(
                    id = -1,
                    emoji = "➕",
                    name = "Add Category",
                    isParent = true
                )

                val fullList = treeItems + addItem

                showCategoryBottomDialog("Category", fullList, edtCategory) {
                    // Sự kiện chỉnh sửa hoặc thêm
                    val bundle = Bundle().apply {
                        putSerializable("selectedType", selectedType)
                    }

                    val fragment = EditItemDialogFragment()
                    fragment.arguments = bundle

                    parentFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right,  // enter
                            R.anim.no_animation,    // exit
                            R.anim.no_animation,    // popEnter (khi quay lại)
                            R.anim.slide_out_right  // popExit (khi quay lại)
                        )
                        .replace(R.id.fragment_container_add_transaction, fragment) // thay fragment container ID
                        .addToBackStack(null)
                        .commit()
                }
            }
        }
    }

    private fun amountTextChangeListener() {
        edtAmount.addTextChangedListener(object : TextWatcher {
            private var isEditing = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isEditing || s == null) return

                isEditing = true

                // Xóa chữ đ nếu có trong chuỗi
                val clean = s.toString().replace("[^\\d]".toRegex(), "")

                if (clean.isNotEmpty()) {
                    try {
                        val parsed = clean.toDouble()
                        val formatted = NumberFormat.getInstance(Locale("vi", "VN")).format(parsed)

                        val finalText = "$formatted đ"

                        // Cập nhật EditText
                        edtAmount.setText(finalText)
                        edtAmount.setSelection(formatted.length) // đặt con trỏ trước chữ đ
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    edtAmount.setText("")
                }

                isEditing = false
            }
        })
    }

    private fun categoryTextChangeListener() {
        edtCategory.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrBlank()) {
                    // Khi đã có text → đổi về màu bình thường
                    val lightBlack = Color.parseColor("#99000000") // 70% opacity black
                    edtCategory.backgroundTintList = ColorStateList.valueOf(lightBlack)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun accountTextChangeListener() {
        edtAccount.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                // Khi đã có text → đổi về màu bình thường
                val lightBlack = Color.parseColor("#99000000") // 70% opacity black
                edtAccount.backgroundTintList = ColorStateList.valueOf(lightBlack)
            }
        })
    }

    private fun switchToAddModeIfEditing() {
        if (isEditMode) {
            showAddMode()
        }
    }
}