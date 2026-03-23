package com.henrystudio.moneymanager.presentation.views.addtransaction

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.henrystudio.moneymanager.databinding.FragmentAddItemBinding
import com.henrystudio.moneymanager.core.util.Helper.Companion.toCategory
import com.henrystudio.moneymanager.data.model.Account
import com.henrystudio.moneymanager.data.model.Category
import com.henrystudio.moneymanager.presentation.model.ItemType
import com.henrystudio.moneymanager.presentation.model.TransactionType
import com.henrystudio.moneymanager.presentation.viewmodel.AccountViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.CategoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddItemFragment : Fragment() {
    private var _binding: FragmentAddItemBinding? = null
    private val biding get() = _binding!!
    private lateinit var nameText: TextView
    private lateinit var btnSave: Button
    private lateinit var itemType: ItemType
    private var transactionType: TransactionType? = null
    private var categoryUpdate: CategoryItem?= null
    private var accountUpdate: EditItem.AccountItem?= null
    private val accountViewModel: AccountViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()
    private val activityViewModel: AddTransactionActivityViewModel by activityViewModels()

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
        itemType = activityViewModel.currentItemType ?: return
        transactionType = activityViewModel.transactionType
        categoryUpdate = arguments?.getSerializable("category_to_edit") as? CategoryItem
        accountUpdate = arguments?.getSerializable("account_to_edit") as? EditItem.AccountItem

        btnSave.setOnClickListener {
            if (nameText.text.trim().isNotEmpty()) {
                when (itemType) {
                    ItemType.CATEGORY -> {
                        val category = categoryUpdate?.toCategory(transactionType?: TransactionType.EXPENSE)?.copy(
                            name = nameText.text.toString(),
                            parentId = categoryUpdate!!.parentId,
                            id = categoryUpdate!!.id
                        ) ?: Category(
                            emoji = "",
                            name = nameText.text.toString(),
                            type = transactionType ?: TransactionType.EXPENSE
                        )
                        if (categoryUpdate != null) {
                            categoryViewModel.update(category)
                        } else {
                            categoryViewModel.insert(category)
                        }
                        activityViewModel.onSaveItem()
                    }
                    ItemType.ACCOUNT -> {
                        Log.d("hieu", "onViewCreated: $accountUpdate")
                        val account = accountUpdate?.item?.copy(
                            name = nameText.text.toString()
                        ) ?: Account(name = nameText.text.toString())
                        if (accountUpdate != null) {
                            accountViewModel.update(account)
                        } else {
                            accountViewModel.insert(account)
                        }
                        activityViewModel.onSaveItem()
                    }
                }
            }
        }
    }

    private fun init(){
        nameText = biding.fragmentAddItemName
        btnSave = biding.fragmentAddItemBtnSave
    }
    companion object {
        private const val KEY_ITEM_TYPE = "item_type"
        private const val KEY_ACTION = "action"

        fun newInstance(
            itemType: ItemType,
            action: AddItemAction
        ): AddItemFragment {
            return AddItemFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(KEY_ITEM_TYPE, itemType) // giữ nguyên nếu ItemType chưa parcelable
                    putParcelable(KEY_ACTION, action)
                }
            }
        }
    }
}