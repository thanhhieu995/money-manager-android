package com.henrystudio.moneymanager.presentation.addtransaction.ui.categoryDetailFragment

import android.os.Bundle
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
import com.henrystudio.moneymanager.databinding.FragmentCategoryDetailBinding
import com.henrystudio.moneymanager.presentation.addtransaction.AddTransactionActivity
import com.henrystudio.moneymanager.presentation.addtransaction.AddTransactionActivityViewModel
import com.henrystudio.moneymanager.presentation.addtransaction.model.AddItemAction
import com.henrystudio.moneymanager.presentation.addtransaction.model.CategoryItem
import com.henrystudio.moneymanager.presentation.addtransaction.model.EditItem
import com.henrystudio.moneymanager.presentation.model.ItemType
import com.henrystudio.moneymanager.presentation.viewmodel.CategoryDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoryDetailFragment : Fragment() {

    private var _binding : FragmentCategoryDetailBinding? = null
    private val binding  get() = _binding!!
    private lateinit var adapter : DetailCategoryAdapter
    private val viewModel: CategoryDetailViewModel by viewModels()
    private val addTransactionActivityViewModel : AddTransactionActivityViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCategoryDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val item = arguments?.getParcelable<EditItem>(KEY_ITEM)
            ?: throw IllegalArgumentException("Missing edit_child_item")
        val action = arguments?.getParcelable<AddItemAction>(KEY_ACTION)
            ?: AddItemAction.FromCategoryDetail
        val recyclerView = binding.fragmentCategoryDetailRecyclerView
        adapter = DetailCategoryAdapter(
            emptyList(),
            onDeleteClick = { deleteChildrenCategory(it.id) },
            onEditClick = { updateChildCategory(it) },
            onItemClick = { childrenCategoryClick(it) })
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        when(item) {
            is EditItem.Category -> {
                viewModel.loadChildCategories(item.id, item.item.parentName, item.item.parentEmoji)
                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        viewModel.uiState.collect { state ->
                            adapter.submitList(state.categoryItems)
                        }
                    }
                }
                (requireActivity() as AddTransactionActivity).selectedCategoryItemForAdd = item.item
            }
            is EditItem.AccountItem -> {
                adapter.submitList(emptyList())
                (requireActivity() as AddTransactionActivity).selectedAccountItemForAdd = item.item
            }
        }
    }

    private fun deleteChildrenCategory(id: Int) {
        viewModel.deleteCategory(id)
    }

    private fun updateChildCategory(categoryItem: CategoryItem) {
        addTransactionActivityViewModel.onAddItemClicked(AddItemAction.FromCategoryDetail, ItemType.CATEGORY, EditItem.Category(categoryItem))
    }

    private fun childrenCategoryClick(categoryItem: CategoryItem) {}

    companion object {
        private const val KEY_ITEM = "edit_child_item"
        private const val KEY_ACTION = "action"
        private const val KEY_TITLE = "title"
        fun newInstance(item: EditItem, action: AddItemAction, title: String): CategoryDetailFragment{
            return CategoryDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(KEY_ITEM, item)
                    putParcelable(KEY_ACTION, action)
                    putSerializable(KEY_TITLE, title)
                }
            }
        }
    }
}