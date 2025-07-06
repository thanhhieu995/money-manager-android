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
import com.example.moneymanager.helper.Helper.Companion.toCategory
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
    private lateinit var source: AddItemSource
    private  var categoryUpdate: CategoryItem?= null
    private var accountUpdate: EditItem.AccountItem?= null

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
        source = arguments?.getSerializable("source") as? AddItemSource ?: AddItemSource.FROM_ADD_TRANSACTION
        categoryUpdate = arguments?.getSerializable("category_to_edit") as? CategoryItem
        accountUpdate = arguments?.getSerializable("account_to_edit") as? EditItem.AccountItem

        if (categoryUpdate != null) {
            nameText.text = categoryUpdate!!.name
            btnSave.text = "Update"
        }

        if (accountUpdate != null) {
            nameText.text = accountUpdate!!.item.name
            btnSave.text = "Update"
        }

        btnSave.setOnClickListener {
            if (nameText.text.trim().isNotEmpty()) {
                (requireActivity() as AddTransactionActivity).updateTransactionTitle(if (categoryType == CategoryType.EXPENSE)"Expense" else "Income")
                (requireActivity() as AddTransactionActivity).updateExtraEditText(if (itemType == ItemType.CATEGORY) "Category" else "Account")
                when (source) {
                    AddItemSource.FROM_ADD_TRANSACTION -> {
                        (requireActivity() as AddTransactionActivity).switchToBookmarkIconWithFade()
                        (requireActivity() as AddTransactionActivity).animateTitleToCenter((requireActivity() as AddTransactionActivity).titleTransaction)
                    }
                    AddItemSource.FROM_EDIT_ITEM_DIALOG -> {
                        (requireActivity() as AddTransactionActivity).switchToAddIconWithFade()
                    }
                }
                (requireActivity() as AddTransactionActivity).animateExtraTextToRight((requireActivity() as AddTransactionActivity).extraAddText)
                when (itemType) {
                    ItemType.CATEGORY -> {
                        // Xử lý cho category
                        val dao = AppDatabase.getDatabase(requireActivity().application).categoryDao()
                        val factory = CategoryViewModelFactory(dao)
                        val viewModel = ViewModelProvider(this, factory)[CategoryViewModel::class.java]

                        val category = categoryUpdate?.toCategory(categoryType)?.copy(
                            name = nameText.text.toString()
                        ) ?: Category(
                            emoji = "",
                            name = nameText.text.toString(),
                            type = categoryType
                        )
                        if (categoryUpdate != null) {
                            viewModel.update(category)
                        } else {
                            viewModel.insert(category)
                        }
                        parentFragmentManager.popBackStack()
                    }
                    ItemType.ACCOUNT -> {
                        // Xử lý cho account
                        val dao = AppDatabase.getDatabase(requireActivity().application).accountDao()
                        val factory = AccountViewModelFactory(dao)
                        val viewModel = ViewModelProvider(this, factory)[AccountViewModel::class.java]
                        val account = accountUpdate?.item?.copy(
                            name = nameText.text.toString()
                        ) ?: Account(name = nameText.text.toString())
                        if (accountUpdate != null) {
                            viewModel.update(account)
                        } else {
                            viewModel.insert(account)
                        }
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