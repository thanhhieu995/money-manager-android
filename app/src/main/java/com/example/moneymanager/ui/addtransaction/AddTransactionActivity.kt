package com.example.moneymanager.ui.addtransaction

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
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

        val edtCategory = findViewById<EditText>(R.id.edtCategory)
        edtCategory.setOnClickListener{
            showCategoryBottomSheet()
        }

//        edtAmount = findViewById(R.id.edtAmount)
//
//        // Gọi focus và bật bàn phím ngay
//        edtAmount.requestFocus()
//
//        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.showSoftInput(edtAmount, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun showCategoryBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_category, null)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewCategory)

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

        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = CategoryAdapter(categories) {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }

}