package com.example.moneymanager.ui.addtransaction

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymanager.R
import com.example.moneymanager.databinding.FragmentEditCategoryBinding
import com.example.moneymanager.helper.Helper.Companion.buildCategoryTree
import com.example.moneymanager.model.AppDatabase
import com.example.moneymanager.model.CategoryType
import com.example.moneymanager.viewmodel.AccountViewModel
import com.example.moneymanager.viewmodel.AccountViewModelFactory
import com.example.moneymanager.viewmodel.CategoryViewModel
import com.example.moneymanager.viewmodel.CategoryViewModelFactory

class EditItemDialogFragment : Fragment() {
    private var _binding : FragmentEditCategoryBinding? = null
    private val binding get() = _binding!!

    private var adapter: EditItemDialogAdapter?= null
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var accountViewModel: AccountViewModel

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

        val layoutSubCategory = binding.fragmentEditCategoryLayoutSubCategory
        val categoryDao = AppDatabase.getDatabase(requireActivity().application).categoryDao()
        val categoryViewModelFactory = CategoryViewModelFactory(categoryDao)
        categoryViewModel = ViewModelProvider(this, categoryViewModelFactory)[CategoryViewModel::class.java]

        val accountDao = AppDatabase.getDatabase(requireActivity().application).accountDao()
        val accountViewModelFactory = AccountViewModelFactory(accountDao)
        accountViewModel = ViewModelProvider(this, accountViewModelFactory)[AccountViewModel::class.java]

        val recyclerView = view.findViewById<RecyclerView>(R.id.fragment_edit_category_recycleView)
        adapter = EditItemDialogAdapter(emptyList(),
        onDeleteClick = {item ->
            when(item) {
                is EditItem.Category -> categoryViewModel.deleteId(item.item.id)
                is EditItem.AccountItem -> accountViewModel.delete(item.item)
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        val selectedType = arguments?.getSerializable("selectedType") as? CategoryType
        if (selectedType == null) {
            layoutSubCategory.visibility = View.GONE
            accountViewModel.getAllAccount().observe(viewLifecycleOwner) { list ->
                val editItems = list.map { EditItem.AccountItem(it) }
                adapter?.submitList(editItems)
            }
        } else {
            layoutSubCategory.visibility = View.VISIBLE
            categoryViewModel.getCategoriesByType(selectedType).observe(viewLifecycleOwner) { list ->
                // xử lý dữ liệu tại đây
                val treeItems = buildCategoryTree(list)
                val editItems = treeItems.map { EditItem.Category(it) }
                adapter?.submitList(editItems)
            }
        }
    }
}