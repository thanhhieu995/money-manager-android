package com.henrystudio.moneymanager.presentation.addtransaction.ui.addItemFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.henrystudio.moneymanager.core.util.Helper.Companion.toCategory
import com.henrystudio.moneymanager.data.model.Account
import com.henrystudio.moneymanager.data.model.Category
import com.henrystudio.moneymanager.databinding.FragmentAddItemBinding
import com.henrystudio.moneymanager.presentation.addtransaction.AddTransactionActivityViewModel
import com.henrystudio.moneymanager.presentation.addtransaction.model.AddItemAction
import com.henrystudio.moneymanager.presentation.addtransaction.model.EditItem
import com.henrystudio.moneymanager.presentation.model.ItemType
import com.henrystudio.moneymanager.presentation.model.TransactionType
import com.henrystudio.moneymanager.presentation.viewmodel.AccountViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.CategoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddItemFragment : Fragment() {
    private var _binding: FragmentAddItemBinding? = null
    private val binding get() = _binding!!

    private lateinit var nameEditText: EditText
    private lateinit var btnSave: Button

    private var itemType: ItemType? = null
    private var editItem: EditItem? = null
    private var transactionType: TransactionType? = null
    private val accountViewModel: AccountViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()
    private val activityViewModel: AddTransactionActivityViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        getDataFromArguments()
        setupUI()
        setupListeners()
    }

    private fun initViews() {
        nameEditText = binding.fragmentAddItemName
        btnSave = binding.fragmentAddItemBtnSave
    }

    private fun getDataFromArguments() {
        itemType = arguments?.getParcelable(KEY_ITEM_TYPE) ?: activityViewModel.currentItemType
        editItem = arguments?.getParcelable(KEY_ITEM_EDIT)
        transactionType = activityViewModel.transactionType
        Log.d("DEBUG", "itemType: $itemType, editItem: $editItem, transactionType: $transactionType")
    }

    private fun setupUI() {
        // Nếu có dữ liệu truyền vào để sửa, hiển thị tên lên EditText
        editItem?.let {
            nameEditText.setText(it.name)
            // Có thể đổi text nút Save thành "Update" nếu muốn
            btnSave.text = "Update"
        } ?: run {
            btnSave.text = "Add"
        }
    }

    private fun setupListeners() {
        btnSave.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            if (name.isEmpty()) {
                nameEditText.error = "Name cannot be empty"
                return@setOnClickListener
            }

            when (itemType) {
                ItemType.CATEGORY -> handleCategorySave(name)
                ItemType.ACCOUNT -> handleAccountSave(name)
                else -> {}
            }
        }
    }

    private fun handleCategorySave(name: String) {
        val currentEditItem = editItem
        if (currentEditItem is EditItem.Category) {
            // Trường hợp Update Category
            val category = currentEditItem.item.toCategory(transactionType ?: TransactionType.EXPENSE).copy(
                name = name,
                emoji = currentEditItem.item.emoji
            )
            categoryViewModel.update(category)
        } else {
            // Trường hợp Insert Category mới
            val category = Category(
                emoji = "", // Bạn có thể thêm logic chọn emoji ở đây
                name = name,
                type = transactionType ?: TransactionType.EXPENSE
            )
            categoryViewModel.insert(category)
        }
        activityViewModel.onSaveItem()
    }

    private fun handleAccountSave(name: String) {
        val currentEditItem = editItem
        if (currentEditItem is EditItem.AccountItem) {
            // Trường hợp Update Account
            val account = currentEditItem.item.copy(name = name)
            accountViewModel.update(account)
        } else {
            // Trường hợp Insert Account mới
            val account = Account(name = name)
            accountViewModel.insert(account)
        }
        activityViewModel.onSaveItem()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val KEY_ITEM_TYPE = "item_type"
        private const val KEY_ACTION = "action"
        private const val KEY_ITEM_EDIT = "item_edit"

        fun newInstance(
            itemType: ItemType,
            action: AddItemAction,
            itemEdit: EditItem? = null
        ): AddItemFragment {
            return AddItemFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(KEY_ITEM_TYPE, itemType)
                    putParcelable(KEY_ACTION, action)
                    putParcelable(KEY_ITEM_EDIT, itemEdit)
                }
            }
        }
    }
}