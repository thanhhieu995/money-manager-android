package com.example.moneymanager.ui.addtransaction

import android.annotation.SuppressLint
import android.app.DatePickerDialog
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
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanager.R
import com.example.moneymanager.model.Transaction
import com.example.moneymanager.viewmodel.CategoryViewModel
import com.example.moneymanager.viewmodel.TransactionViewModel
import com.example.moneymanager.databinding.FragmentAddTransactionBinding
import com.example.moneymanager.helper.Helper
import com.example.moneymanager.model.AppDatabase
import com.example.moneymanager.model.Category
import com.example.moneymanager.model.CategoryType
import com.example.moneymanager.ui.main.MainActivity
import com.example.moneymanager.viewmodel.CategoryViewModelFactory
import com.example.moneymanager.viewmodel.TransactionViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionFragment : Fragment() {

    private var _binding: FragmentAddTransactionBinding?= null
    private val binding get() = _binding!!

    private lateinit var viewModel: TransactionViewModel
    private lateinit var categoryViewModel: CategoryViewModel
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

        edtAccount.setOnClickListener {
            showBottomDialogAddTransaction("Account", accounts, edtAccount) {
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
                    note = edtNote.text.toString(),
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
                note = edtNote.text.toString(),
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

    @SuppressLint("MissingInflatedId")
    private fun showBottomDialogAddTransaction(
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
            if(title == "Category") {
                val parentEmoji = selectedItem.parentEmoji ?: ""
                val parentName = if (selectedItem.parentName == null) "" else selectedItem.parentName + "/"
                targetEditText.setText("$parentEmoji $parentName ${selectedItem.emoji} ${selectedItem.name}")
            } else {
                targetEditText.setText("${selectedItem.parentEmoji} ${selectedItem.parentName} ${selectedItem.emoji} ${selectedItem.name}")
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
        layoutSave.visibility = View.VISIBLE
        layoutEdit.visibility = View.GONE
        continueButton.visibility = View.VISIBLE
        if (!isEditMode){
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
            AlertDialog.Builder(requireContext())
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Yes") { dialog, _ ->
                    viewModel.delete(transaction)
                    Toast.makeText(context, "Transaction deleted", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    requireActivity().supportFragmentManager.popBackStack() // Thoát fragment
                }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        bookMarkButton.setOnClickListener {
            val updated = transaction.copy(isBookmarked = true)
            viewModel.update(updated)
            Toast.makeText(context, "Bookmarked!", Toast.LENGTH_SHORT).show()
            requireActivity().supportFragmentManager.popBackStack()
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

                showBottomDialogAddTransaction("Category", fullList, edtCategory) {
                    // Sự kiện chỉnh sửa hoặc thêm
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

    private fun buildCategoryTree(categories: List<Category>): List<CategoryItem> {
        val parentItems = mutableListOf<CategoryItem>()

        categories.filter { it.parentId == null }.forEach { parent ->
            val children = categories.filter { it.parentId == parent.id }
                .map { child ->
                    CategoryItem(
                        id = child.id,
                        emoji = child.emoji,
                        name = child.name,
                        isParent = false,
                        parentName = parent.name,
                        parentEmoji = parent.emoji
                    )
                }

            val parentItem = CategoryItem(
                id = parent.id,
                emoji = parent.emoji,
                name = parent.name,
                isParent = true,
                children = children
            )

            parentItems.add(parentItem)
        }

        return parentItems
    }

    private fun switchToAddModeIfEditing() {
        if (isEditMode) {
            showAddMode()
        }
    }
}