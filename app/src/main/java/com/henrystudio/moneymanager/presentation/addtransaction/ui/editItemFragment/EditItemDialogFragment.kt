package com.henrystudio.moneymanager.presentation.addtransaction.ui.editItemFragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.data.model.Account
import com.henrystudio.moneymanager.databinding.FragmentEditCategoryBinding
import com.henrystudio.moneymanager.presentation.addtransaction.AddTransactionActivityViewModel
import com.henrystudio.moneymanager.presentation.addtransaction.components.adapter.EditItemDialogAdapter
import com.henrystudio.moneymanager.presentation.addtransaction.model.AddItemAction
import com.henrystudio.moneymanager.presentation.addtransaction.model.CategoryItem
import com.henrystudio.moneymanager.presentation.addtransaction.model.EditItem
import com.henrystudio.moneymanager.presentation.model.ItemType
import com.henrystudio.moneymanager.presentation.model.TransactionType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditItemDialogFragment: Fragment(), EditItemDialogAdapter.OnEditClickListener {
    private var _binding : FragmentEditCategoryBinding? = null
    private val binding get() = _binding!!

    private var adapter: EditItemDialogAdapter? = null
    private val viewModel: EditItemDialogViewModel by viewModels()
    private val addTransactionActivityViewModel : AddTransactionActivityViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEditCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.fragment_edit_category_recycleView)
        adapter = EditItemDialogAdapter(
            emptyList(),
            onDeleteClick = { item -> viewModel.deleteItem(item) },
            clickItemListener = this
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        val selectedType = arguments?.getParcelable<ItemType>(KEY_ITEM_TYPE)
        val transactionType = arguments?.getParcelable<TransactionType>(KEY_TRANSACTION_TYPE)
        val action = arguments?.getParcelable<AddItemAction>(KEY_ACTION)
        Log.d("DEBUG", "onViewCreated selectedType: $selectedType transactionType: $transactionType action: $action")
        viewModel.setType(selectedType, transactionType)
//        addTransactionActivityViewModel.apply {
//            currentItemType = selectedType
//            currentAction = action
//        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.editItems.collect{ list ->
                    adapter?.submitList(list)
                }
            }
        }
    }

    override fun onEditItemClick(item: EditItem) {
        when(item) {
            is EditItem.Category -> {
                // 🔥 FIX QUAN TRỌNG: set parentId
                addTransactionActivityViewModel.currentParentCategoryId = item.item.id

                addTransactionActivityViewModel.onRootCategoryItemClicked(item.item, AddItemAction.FromCategoryDetail)
            }
            is EditItem.AccountItem -> {
                addTransactionActivityViewModel.onEditItemClicked(AddItemAction.FromEditAccount, ItemType.ACCOUNT)
            }
        }
    }

    companion object {
        private const val KEY_ITEM_TYPE = "item_type"
        private const val KEY_TRANSACTION_TYPE = "transaction_type"
        private const val KEY_ACTION = "action"

        fun newInstance(
            itemType: ItemType,
            transactionType: TransactionType,
            action: AddItemAction
        ): EditItemDialogFragment {
            return EditItemDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(KEY_ITEM_TYPE, itemType) // category or account
                    putParcelable(KEY_TRANSACTION_TYPE, transactionType)
                    putParcelable(KEY_ACTION, action)
                }
            }
        }
    }
}