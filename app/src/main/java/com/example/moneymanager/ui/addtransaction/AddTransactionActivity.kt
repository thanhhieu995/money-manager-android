package com.example.moneymanager.ui.addtransaction

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanager.R
import com.example.moneymanager.helper.Currency
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

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        val btnBack = findViewById<ImageButton>(R.id.add_transaction_btn_back)
        val dateTextView = findViewById<TextView>(R.id.add_transaction_date)
        val incomeButton = findViewById<Button>(R.id.add_transaction_btn_income)
        val expenseButton = findViewById<Button>(R.id.add_transaction_btn_expense)
        val titleTransaction = findViewById<TextView>(R.id.add_transaction_title)
        val edtAmount = findViewById<EditText>(R.id.add_transaction_amount)
        val edtCategory = findViewById<EditText>(R.id.add_transaction_edtCategory)
        val edtAccount = findViewById<EditText>(R.id.add_transaction_edtAccount)
        val edtNote = findViewById<TextView>(R.id.add_transaction_edtNote)
        val saveButton = findViewById<Button>(R.id.add_transaction_btnSave)

        val dao = AppDatabase.getDatabase(application).transactionDao()
        val factory = TransactionViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]

        btnBack.setOnClickListener{
            finish()
        }


// Lấy ngày hiện tại và hiển thị
        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd/MM/yy (EEE)", Locale.getDefault())
        val formattedDate = dateFormat.format(currentDate)
        dateTextView.text = formattedDate

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
            isIncome = true
            incomeButton.setBackgroundColor(Color.BLUE)
            incomeButton.setTextColor(Color.WHITE)
            expenseButton.setBackgroundColor(Color.WHITE)
            expenseButton.setTextColor(Color.BLACK)
            saveButton.setBackgroundColor(Color.BLUE)
            saveButton.setTextColor(Color.WHITE)
            titleTransaction.text = "Income"
        }

        expenseButton.setOnClickListener {
            isIncome = false
            incomeButton.setBackgroundColor(Color.WHITE)
            incomeButton.setTextColor(Color.BLACK)
            expenseButton.setBackgroundColor(Color.RED)
            expenseButton.setTextColor(Color.WHITE)
            saveButton.setBackgroundColor(Color.RED)
            titleTransaction.text = "Expense"
        }

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

        edtCategory.setOnClickListener{
            showBottomDialogAddTransaction("Category", categories, edtCategory) {

            }
        }

        edtAccount.setOnClickListener {
            showBottomDialogAddTransaction("Account" ,accounts, edtAccount) {

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
            if (edtAmount.text.isEmpty() || edtCategory.text.isEmpty() || edtAccount.text.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ Amount, Category và Account", Toast.LENGTH_SHORT).show()
            } else {
                val amount = edtAmount.text.toString()
                    .replace("[^\\d]".toRegex(), "")
                    .toDoubleOrNull() ?: 0.0
                val transaction = Transaction(
                    title = titleTransaction.text.toString(),
                    category = edtCategory.text.toString(),
                    content = edtNote.text.toString(),
                    amount = amount,
                    isIncome = isIncome,
                    date = dateTextView.text.toString()
                )
                viewModel.insert(transaction)

                // Nếu đủ thông tin thì chuyển sang MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // kết thúc activity hiện tại
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun showBottomDialogAddTransaction(title : String, itemBottoms: List<ItemBottomDialog>,targetEditText: EditText , onEditClick: () -> Unit) {
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
}