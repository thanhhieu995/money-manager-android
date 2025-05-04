package com.example.moneymanager.ui.addtransaction

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanager.R
import com.example.moneymanager.ui.main.MainActivity
import com.google.android.material.bottomsheet.BottomSheetDialog

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var edtAmount: EditText
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        val btnBack = findViewById<ImageButton>(R.id.add_transaction_btn_back)
        btnBack.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val tvTitle = findViewById<TextView>(R.id.add_transaction_title)

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

        val edtCategory = findViewById<EditText>(R.id.add_transaction_edtCategory)
        edtCategory.setOnClickListener{
            showBottomDialogAddTransaction("Category", categories, edtCategory) {

            }
        }

        val edtAccount = findViewById<EditText>(R.id.add_transaction_edtAccount)
        edtAccount.setOnClickListener {
            showBottomDialogAddTransaction("Account" ,accounts, edtAccount) {

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