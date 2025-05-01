package com.example.moneymanager.ui.addtransaction

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanager.R
import com.example.moneymanager.ui.main.MainActivity
import com.google.android.material.bottomsheet.BottomSheetDialog

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var edtAmount: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val categories = listOf(
            Category("🍔", "Food"),
            Category("🚗", "Transport"),
            Category("🏠", "Household"),
            Category("🐶", "Pets"),
            Category("🎁", "Gift"),
            Category("📚", "Education"),
            Category("🏋️‍♂️", "Sport"),
            Category("💄", "Beauty"),
            Category("🧘‍♂️", "Health"),
            Category("💻", "Investment"),
            Category("🎨", "Culture"),
            Category("🚲", "Bicycle")
        )

        val accounts = listOf(
            Category("💵", "Cash"),
            Category("🏦", "Bank Account"),
            Category("💳", "Credit Card"),
            Category("📱", "E-Wallet"),
            Category("🪙", "Crypto"),
            Category("📦", "Savings"),
            Category("➕", "Add")
        )

        val edtCategory = findViewById<EditText>(R.id.edtCategory)
        edtCategory.setOnClickListener{
            showBottomDialogAddTransaction("Category", categories)
        }

        val edtAccount = findViewById<EditText>(R.id.edtAccount)
        edtAccount.setOnClickListener {
            showBottomDialogAddTransaction("Account" ,accounts)
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun showBottomDialogAddTransaction(title : String, categories: List<Category>) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_dialog_add, null)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewDialog)
        val titleBottom = view.findViewById<TextView>(R.id.tvTitleBottom)
        titleBottom.text = title

        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = CategoryAdapter(categories) {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }
}