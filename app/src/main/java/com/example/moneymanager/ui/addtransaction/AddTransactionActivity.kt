package com.example.moneymanager.ui.addtransaction

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanager.R
import com.example.moneymanager.model.AppDatabase
import com.example.moneymanager.model.Transaction
import com.example.moneymanager.ui.main.MainActivity
import com.example.moneymanager.viewmodel.TransactionViewModel
import com.example.moneymanager.viewmodel.TransactionViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var viewModel: TransactionViewModel
    private var isIncome: Boolean = false
    val currency = com.example.moneymanager.helper.Currency()
    private var transactions: List<Transaction> = listOf()
    private lateinit var btnBack: ImageView
    private lateinit var dateTextView: TextView
    private lateinit var incomeButton: Button
    private lateinit var expenseButton: Button
    private lateinit var titleTransaction: TextView
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

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        init()
        // Lấy ngày hiện tại và hiển thị
        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd/MM/yy (EEE)", Locale.getDefault())
        formattedDate = dateFormat.format(currentDate)

        val categories = listOf(
            ItemBottomDialog("🍔", "Food"),
            ItemBottomDialog("🚗", "Transport"),
            ItemBottomDialog("🏠", "Household"),
            ItemBottomDialog("🐶", "Pets"),
            ItemBottomDialog("🎁", "Gift"),
            ItemBottomDialog("📚", "Education"),
            ItemBottomDialog("🏋️‍♂️", "Sport"),
            ItemBottomDialog("💄", "Beauty"),
            ItemBottomDialog("🧘‍♂️", "Health"),
            ItemBottomDialog("💻", "Investment"),
            ItemBottomDialog("🎨", "Culture"),
            ItemBottomDialog("🚲", "Bicycle")
        )

        val accounts = listOf(
            ItemBottomDialog("💵", "Cash"),
            ItemBottomDialog("🏦", "Bank Account"),
            ItemBottomDialog("💳", "Credit Card"),
            ItemBottomDialog("📱", "E-Wallet"),
            ItemBottomDialog("🪙", "Crypto"),
            ItemBottomDialog("📦", "Savings"),
        )

        val dao = AppDatabase.getDatabase(application).transactionDao()
        val factory = TransactionViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]

        handleToAddTransaction()

        btnBack.setOnClickListener {
            finish()
        }

        dateTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val pickedCalendar = Calendar.getInstance()
                pickedCalendar.set(selectedYear, selectedMonth, selectedDay)
                val newFormattedDate = dateFormat.format(pickedCalendar.time)
                dateTextView.text = newFormattedDate
            }, year, month, day)

            datePicker.show()
        }

        incomeButton.setOnClickListener {
            setTransactionType(
                true,
                false
            )
        }

        expenseButton.setOnClickListener {
            setTransactionType(
                false,
                false
            )
        }

        edtCategory.setOnClickListener {
            showBottomDialogAddTransaction("Category", categories, edtCategory) {

            }
        }

        edtAccount.setOnClickListener {
            showBottomDialogAddTransaction("Account", accounts, edtAccount) {

            }
        }

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

        saveButton.setOnClickListener {
            saveTransaction {
                // Nếu đủ thông tin thì chuyển sang MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // kết thúc activity hiện tại
            }
        }

        continueButton.setOnClickListener {
            saveTransaction {
                Toast.makeText(this, "Giao dịch đã được lưu!", Toast.LENGTH_SHORT).show()

                // Reset các trường
                edtAmount.setText("")
                edtCategory.setText("")
                edtAccount.setText("")
                edtNote.setText("")

                edtAmount.requestFocus()
            }
        }

        // xu ly goi y khi input vao note
        viewModel.allTransactions.observe(this) {
            transactions = it

            val contents = transactions.map { it.note }.distinct()

            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                contents
            )

            edtNote.setAdapter(adapter)
        }
    }

    private fun init() {
        btnBack = findViewById<ImageButton>(R.id.add_transaction_btn_back)
        dateTextView = findViewById(R.id.add_transaction_date)
        incomeButton = findViewById(R.id.add_transaction_btn_income)
        expenseButton = findViewById(R.id.add_transaction_btn_expense)
        titleTransaction = findViewById(R.id.add_transaction_title)
        edtAmount = findViewById(R.id.add_transaction_amount)
        edtCategory = findViewById(R.id.add_transaction_edtCategory)
        edtAccount = findViewById(R.id.add_transaction_edtAccount)
        edtNote = findViewById(R.id.add_transaction_edtNote)
        saveButton = findViewById(R.id.add_transaction_btnSave)
        continueButton = findViewById(R.id.add_transaction_btnContinue)
        layoutSave = findViewById(R.id.add_transaction_layout_save)
        layoutEdit = findViewById(R.id.add_transaction_layout_edit)
        deleteButton = findViewById(R.id.add_transaction_btnDelete)
        copyButton = findViewById(R.id.add_transaction_btnCopy)
        bookMarkButton = findViewById(R.id.add_transaction_btnBookmark)
    }

    private fun saveTransaction(onSuccess: () -> Unit) {
        if (edtAmount.text.isEmpty() || edtCategory.text.isEmpty() || edtAccount.text.isEmpty()) {
            Toast.makeText(
                this,
                "Vui lòng nhập đầy đủ Amount, Category và Account",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val amount = edtAmount.text.toString()
            .replace("[^\\d]".toRegex(), "")
            .toDoubleOrNull() ?: 0.0

        val transaction = Transaction(
            title = titleTransaction.text.toString(),
            category = edtCategory.text.toString(),
            note = edtNote.text.toString(),
            account = edtAccount.text.toString(),
            amount = amount,
            isIncome = isIncome,
            date = dateTextView.text.toString()
        )

        viewModel.insert(transaction)
        onSuccess()
    }

    @SuppressLint("MissingInflatedId")
    private fun showBottomDialogAddTransaction(
        title: String,
        itemBottoms: List<ItemBottomDialog>,
        targetEditText: EditText,
        onEditClick: () -> Unit
    ) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_dialog_add, null)
        val recyclerView = view.findViewById<RecyclerView>(R.id.bottom_dialog_add_recyclerView)
        val titleBottom = view.findViewById<TextView>(R.id.bottom_dialog_add_title)
        val editButton = view.findViewById<ImageButton>(R.id.bottom_dialog_add_btn_edit)
        val closeButton = view.findViewById<ImageButton>(R.id.bottom_dialog_add_btn_close)
        titleBottom.text = title

        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = ItemBottomAdapter(itemBottoms) { selectedItem ->
            targetEditText.setText("${selectedItem.emoji} ${selectedItem.name}")
            bottomSheetDialog.dismiss()
        }

        editButton.setOnClickListener {
            bottomSheetDialog.dismiss()
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

        titleTransaction.text = if (isIncomeType) "Income" else "Expense"

        layoutSave.visibility = if (isEdit) View.GONE else View.VISIBLE
        layoutEdit.visibility = if (isEdit) View.VISIBLE else View.GONE
    }

    private fun handleToAddTransaction() {
        val transaction = intent.getSerializableExtra("transaction") as? Transaction

        if (transaction == null) {
            showAddMode()
        } else {
            showEditMode(transaction)
        }
    }

    private fun showAddMode() {
        layoutSave.visibility = View.VISIBLE
        layoutEdit.visibility = View.GONE
        continueButton.visibility = View.VISIBLE
        dateTextView.text = formattedDate
    }

    private fun showEditMode(transaction: Transaction) {
        continueButton.visibility = View.GONE
        layoutSave.visibility = View.GONE
        layoutEdit.visibility = View.VISIBLE

        populateTransactionFields(transaction)
        setTransactionType(transaction.isIncome, true)

        copyButton.setOnClickListener {
            layoutSave.visibility = View.VISIBLE
            layoutEdit.visibility = View.GONE
            populateTransactionFields(transaction)
            dateTextView.text = formattedDate
        }

        deleteButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Yes") { dialog, _ ->
                    viewModel.delete(transaction)
                    Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    finish()
                }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        bookMarkButton.setOnClickListener {
            val updated = transaction.copy(isBookmarked = true)
            viewModel.update(updated)
            Toast.makeText(this, "Bookmarked!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun populateTransactionFields(transaction: Transaction) {
        edtAmount.setText(currency.formatCurrency(transaction.amount))
        edtCategory.setText(transaction.category)
        edtAccount.setText(transaction.account)
        edtNote.setText(transaction.note)
        dateTextView.text = transaction.date
    }
}