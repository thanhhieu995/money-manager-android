package com.henrystudio.moneymanager.ui.addtransaction

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.henrystudio.moneymanager.databinding.FragmentAddItemBinding
import com.henrystudio.moneymanager.helper.Helper.Companion.toCategory
import com.henrystudio.moneymanager.model.*
import com.henrystudio.moneymanager.viewmodel.AccountViewModel
import com.henrystudio.moneymanager.viewmodel.AccountViewModelFactory
import com.henrystudio.moneymanager.viewmodel.CategoryViewModel
import com.henrystudio.moneymanager.viewmodel.CategoryViewModelFactory

class AddItemFragment : Fragment() {

    private var _binding: FragmentAddItemBinding? = null
    private val biding get() = _binding!!
    private lateinit var nameText: TextView
    private lateinit var btnSave: Button
    private lateinit var itemType: ItemType
    private lateinit var categoryType: CategoryType
    private lateinit var source: AddItemSource
    private var categoryUpdate: CategoryItem?= null
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

        when {
            categoryUpdate != null -> {
                nameText.text = categoryUpdate!!.name
                btnSave.text = "Update"
                (requireActivity() as AddTransactionActivity).addIcon.visibility = View.GONE
            }
            accountUpdate != null -> {
                nameText.text = accountUpdate!!.item.name
                btnSave.text = "Update"
                (requireActivity() as AddTransactionActivity).addIcon.visibility = View.GONE
            }
        }

        btnSave.setOnClickListener {
            if (nameText.text.trim().isNotEmpty()) {
                (requireActivity() as AddTransactionActivity).updateTransactionTitle(if (categoryType == CategoryType.EXPENSE)"Expense" else "Income")
                (requireActivity() as AddTransactionActivity).updateTitleIncoming(if (itemType == ItemType.CATEGORY) "Category" else "Account")
                when (source) {
                    AddItemSource.FROM_ADD_TRANSACTION -> {
                        (requireActivity() as AddTransactionActivity).switchToBookmarkIconWithFade()
                        (requireActivity() as AddTransactionActivity).animateTitleToCenter((requireActivity() as AddTransactionActivity).titleCurrent)
                    }
                    AddItemSource.FROM_EDIT_ITEM_CATEGORY_DIALOG -> {
                        (requireActivity() as AddTransactionActivity).switchToAddIconWithFade()
                    }
                    AddItemSource.FROM_DETAIL_CATEGORY -> {}
                    AddItemSource.FROM_EDIT_ITEM_ACCOUNT_DIALOG -> {}
                }
                when (itemType) {
                    ItemType.CATEGORY -> {
                        // Xử lý cho category
                        val dao = AppDatabase.getDatabase(requireActivity().application).categoryDao()
                        val factory = CategoryViewModelFactory(dao)
                        val viewModel = ViewModelProvider(this, factory)[CategoryViewModel::class.java]

                        val category = categoryUpdate?.toCategory(categoryType)?.copy(
                            name = nameText.text.toString(),
                            parentId = categoryUpdate!!.parentId,
                            id = categoryUpdate!!.id
                        ) ?: Category(
                            emoji = "",
                            name = nameText.text.toString(),
                            type = categoryType
                        )
                        if (categoryUpdate != null) {
                            viewModel.update(category)
                        } else if (source == AddItemSource.FROM_DETAIL_CATEGORY) {
                            val categoryChoose = (requireActivity() as AddTransactionActivity).selectedCategoryItemForAdd
                            val childCategory = Category(emoji = "", name = nameText.text.toString(), parentId = categoryChoose?.id, type = categoryType)
                            viewModel.insert(childCategory)
                        } else {
                            viewModel.insert(category)
                        }
                        (requireActivity() as AddTransactionActivity).popTitleStackAndAnimateBack()
                        (requireActivity() as AddTransactionActivity).addIcon.visibility = View.VISIBLE
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
                        (requireActivity() as AddTransactionActivity).popTitleStackAndAnimateBack()
                        (requireActivity() as AddTransactionActivity).addIcon.visibility = View.VISIBLE
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