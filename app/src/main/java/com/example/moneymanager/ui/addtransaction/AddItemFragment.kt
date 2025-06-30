package com.example.moneymanager.ui.addtransaction

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.moneymanager.databinding.FragmentAddItemBinding
import com.example.moneymanager.model.*
import com.example.moneymanager.viewmodel.AccountViewModel
import com.example.moneymanager.viewmodel.AccountViewModelFactory
import com.example.moneymanager.viewmodel.CategoryViewModel
import com.example.moneymanager.viewmodel.CategoryViewModelFactory

class AddItemFragment : Fragment() {

    private var _binding: FragmentAddItemBinding? = null
    private val biding get() = _binding!!
    private lateinit var nameText: TextView
    private lateinit var btnSave: Button
    private lateinit var itemType: ItemType
    private lateinit var categoryType: CategoryType

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAddItemBinding.inflate(inflater, container, false)
        return biding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        itemType = arguments?.getSerializable("item_type") as? ItemType ?: ItemType.CATEGORY
        categoryType = arguments?.getSerializable("category_type") as? CategoryType ?: CategoryType.EXPENSE

        btnSave.setOnClickListener {
            if (nameText.text.trim().isNotEmpty()) {
                when (itemType) {
                    ItemType.CATEGORY -> {
                        // Xử lý cho category
                        val dao = AppDatabase.getDatabase(requireActivity().application).categoryDao()
                        val factory = CategoryViewModelFactory(dao)
                        val viewModel = ViewModelProvider(this, factory)[CategoryViewModel::class.java]
                        val category = Category(
                            emoji = "",
                            name = nameText.text.toString(),
                            type = categoryType
                        )
                        viewModel.insert(category)
                        parentFragmentManager.popBackStack()
                    }
                    ItemType.ACCOUNT -> {
                        // Xử lý cho account
                        val dao = AppDatabase.getDatabase(requireActivity().application).accountDao()
                        val factory = AccountViewModelFactory(dao)
                        val viewModel = ViewModelProvider(this, factory)[AccountViewModel::class.java]
                        val account = Account(name = nameText.text.toString())
                        viewModel.insert(account)
                        parentFragmentManager.popBackStack()
                    }
                }
            }
        }
    }

    private fun init(){
        nameText = biding.fragmentAddItemName
        btnSave = biding.fragmentAddItemBtnSave
    }
}