package com.example.moneymanager.ui.addtransaction

import android.os.Bundle
import android.util.Log
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
import com.example.moneymanager.viewmodel.CategoryViewModel
import com.example.moneymanager.viewmodel.CategoryViewModelFactory

class EditCategoryFragment : Fragment() {
    private var _binding : FragmentEditCategoryBinding? = null
    private val binding get() = _binding!!

    private var adapter: EditCategoryAdapter?= null
    private lateinit var viewModel: CategoryViewModel

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

        val dao = AppDatabase.getDatabase(requireActivity().application).categoryDao()
        val factory = CategoryViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[CategoryViewModel::class.java]
        val recyclerView = view.findViewById<RecyclerView>(R.id.fragment_edit_category_recycleView)
        adapter = EditCategoryAdapter(emptyList(),
        onDeleteClick = {categoryItem ->
            viewModel.deleteId(categoryItem.id)
        })

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        val selectedType = arguments?.getSerializable("selectedType") as? CategoryType
        selectedType?.let { type ->
            viewModel.getCategoriesByType(type).observe(viewLifecycleOwner) { list ->
                // xử lý dữ liệu tại đây
                val treeItems = buildCategoryTree(list)
                adapter?.submitList(treeItems)
            }
        }
    }
}