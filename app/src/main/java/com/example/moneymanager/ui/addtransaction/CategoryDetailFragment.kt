package com.example.moneymanager.ui.addtransaction

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneymanager.databinding.FragmentCategoryDetailBinding

class CategoryDetailFragment : Fragment() {

    private var _binding : FragmentCategoryDetailBinding? = null
    private val binding  get() = _binding!!
    private lateinit var adapter : DetailCategoryAdapter

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
        Log.d("hieu", "item: $item")
        val recyclerView = binding.fragmentCategoryDetailRecyclerView
        adapter = DetailCategoryAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        if (item is EditItem.Category) {
            val list = item.item.children
            adapter.submitList(list)
        }
    }
}