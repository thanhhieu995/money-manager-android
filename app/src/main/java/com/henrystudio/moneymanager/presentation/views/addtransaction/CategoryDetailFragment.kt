package com.henrystudio.moneymanager.presentation.views.addtransaction

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.databinding.FragmentCategoryDetailBinding
import com.henrystudio.moneymanager.presentation.model.AddItemSource
import com.henrystudio.moneymanager.data.local.AppDatabase
import com.henrystudio.moneymanager.presentation.model.ItemType
import com.henrystudio.moneymanager.presentation.viewmodel.AccountViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.AccountViewModelFactory
import com.henrystudio.moneymanager.presentation.viewmodel.CategoryViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.CategoryViewModelFactory

class CategoryDetailFragment : Fragment() {

    private var _binding : FragmentCategoryDetailBinding? = null
    private val binding  get() = _binding!!
    private lateinit var adapter : DetailCategoryAdapter
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var accountViewModel : AccountViewModel

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

        val categoryDao = AppDatabase.getDatabase(requireActivity().application).categoryDao()
        val categoryViewModelFactory = CategoryViewModelFactory(categoryDao)
        categoryViewModel = ViewModelProvider(this, categoryViewModelFactory)[CategoryViewModel::class.java]

        val accountDao = AppDatabase.getDatabase(requireActivity().application).accountDao()
        val accountViewModelFactory = AccountViewModelFactory(accountDao)
        accountViewModel = ViewModelProvider(this, accountViewModelFactory)[AccountViewModel::class.java]

        val item = arguments?.getSerializable("edit_child_item") as EditItem
        val recyclerView = binding.fragmentCategoryDetailRecyclerView
        adapter = DetailCategoryAdapter(emptyList(),
        onDeleteClick = {deleteChildrenCategory(it.id)},
        onItemClick = {childrenCategoryClick(it)})
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        when(item) {
            is EditItem.Category -> {
                categoryViewModel.getChildCategories(item.id).observe(viewLifecycleOwner) { list ->
                    val categoryItemList = list.map { category ->
                        CategoryItem(
                            id = category.id,
                            name = category.name,
                            emoji = category.emoji,
                            parentId = category.parentId ?: -1,
                            isParent = false,
                            parentName = item.item.parentName,
                            parentEmoji = item.item.parentEmoji
                        )
                    }
                    adapter.submitList(categoryItemList)
                }
                (requireActivity() as AddTransactionActivity).selectedCategoryItemForAdd = item.item
            }
            is EditItem.AccountItem -> {
                adapter.submitList(emptyList())
                (requireActivity() as AddTransactionActivity).selectedAccountItemForAdd = item.item
            }
        }
    }

    private fun deleteChildrenCategory(id : Int){
        categoryViewModel.deleteId(id)
    }

    private fun childrenCategoryClick(categoryItem: CategoryItem) {
        val title = (requireActivity() as AddTransactionActivity).titleCurrent
        (requireActivity() as AddTransactionActivity).animateTitleToLeftOfIcon(title)
        val titleIncoming = (requireActivity() as AddTransactionActivity).titleIncoming
        (requireActivity() as AddTransactionActivity).animateIncomingTitleToCenter(titleIncoming, "Edit")
        (requireActivity() as AddTransactionActivity).titleStack.addLast(title.text.toString())
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