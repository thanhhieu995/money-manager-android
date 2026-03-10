package com.henrystudio.moneymanager.ui.addtransaction

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.databinding.FragmentEditCategoryBinding
import com.henrystudio.moneymanager.core.util.Helper.Companion.buildCategoryTree
import com.henrystudio.moneymanager.model.AddItemSource
import com.henrystudio.moneymanager.core.database.AppDatabase
import com.henrystudio.moneymanager.model.CategoryType
import com.henrystudio.moneymanager.model.ItemType
import com.henrystudio.moneymanager.viewmodel.AccountViewModel
import com.henrystudio.moneymanager.viewmodel.AccountViewModelFactory
import com.henrystudio.moneymanager.viewmodel.CategoryViewModel
import com.henrystudio.moneymanager.viewmodel.CategoryViewModelFactory

class EditItemDialogFragment : Fragment(), EditItemDialogAdapter.OnEditClickListener {
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
        },
        clickItemListener = this)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        val selectedType = arguments?.getSerializable("selectedType") as? CategoryType
        if (selectedType == null) {
            accountViewModel.getAllAccount().observe(viewLifecycleOwner) { list ->
                val editItems = list.map { EditItem.AccountItem(it) }
                adapter?.submitList(editItems)
            }
        } else {
            categoryViewModel.getCategoriesByType(selectedType).observe(viewLifecycleOwner) { list ->
                // xử lý dữ liệu tại đây
                val treeItems = buildCategoryTree(list)
                val editItems = treeItems.map { EditItem.Category(it) }
                adapter?.submitList(editItems)
            }
        }
    }

    override fun onEditItemClick(item: EditItem) {
        val title = (requireActivity() as AddTransactionActivity).titleCurrent
        (requireActivity() as AddTransactionActivity).animateTitleToLeftOfIcon(title)
        (requireActivity() as AddTransactionActivity).updateTitleIncoming(item.name)
        val titleIncoming = (requireActivity() as AddTransactionActivity).titleIncoming
        (requireActivity() as AddTransactionActivity).animateIncomingTitleToCenter(titleIncoming, item.name)
        (requireActivity() as AddTransactionActivity).titleStack.addLast(title.text.toString())
        when(item) {
            is EditItem.Category -> {
                val fragment = CategoryDetailFragment()
                val bundle = Bundle().apply {
                    putSerializable("edit_child_item", item)
                    putSerializable("item_type", ItemType.CATEGORY)
                    putSerializable("source", AddItemSource.FROM_EDIT_ITEM_CATEGORY_DIALOG)
                }
                fragment.arguments = bundle
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right,  // enter
                        R.anim.no_animation,    // exit
                        R.anim.no_animation,    // popEnter (khi quay lại)
                        R.anim.slide_out_right  // popExit (khi quay lại)
                    )
                    .replace(R.id.fragment_container_add_transaction, fragment) // thay fragment container ID
                    .addToBackStack(null)
                    .commit()
            }
            is EditItem.AccountItem -> {
                (requireActivity() as AddTransactionActivity).addIcon.visibility = View.GONE
                val fragment = AddItemFragment()
                val bundle = Bundle().apply {
                    putSerializable("item_type", ItemType.ACCOUNT)
                    putSerializable("account_to_edit", item)
                    putSerializable("source", AddItemSource.FROM_EDIT_ITEM_ACCOUNT_DIALOG)
                }
                fragment.arguments = bundle
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(
                    R.anim.slide_in_right,  // enter
                    R.anim.no_animation,    // exit
                    R.anim.no_animation,    // popEnter (khi quay lại)
                    R.anim.slide_out_right  // popExit (khi quay lại)
                    )
                    .replace(R.id.fragment_container_add_transaction, fragment) // thay fragment container ID
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
}