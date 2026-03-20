package com.henrystudio.moneymanager.presentation.views.addtransaction

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.databinding.FragmentCategoryDetailBinding
import com.henrystudio.moneymanager.presentation.model.AddItemSource
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

        val item = arguments?.getSerializable("edit_child_item") as EditItem
        val recyclerView = binding.fragmentCategoryDetailRecyclerView
        adapter = DetailCategoryAdapter(emptyList(),
        onDeleteClick = {deleteChildrenCategory(it.id)},
        onItemClick = {childrenCategoryClick(it)})
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

    private fun childrenCategoryClick(categoryItem: CategoryItem) {
        val fragment = AddItemFragment().apply {
            arguments = Bundle().apply {
                putSerializable("item_type", ItemType.CATEGORY)
                putSerializable("source", AddItemSource.FROM_DETAIL_CATEGORY)
                putSerializable("category_to_edit", categoryItem)
            }
        }
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,  // enter
                R.anim.no_animation,    // exit
                R.anim.no_animation,    // popEnter (khi quay lại)
                R.anim.slide_out_right  // popExit (khi quay lại)
            ).replace(R.id.fragment_container_add_transaction, fragment)
            .addToBackStack(null)
            .commit()
    }
}