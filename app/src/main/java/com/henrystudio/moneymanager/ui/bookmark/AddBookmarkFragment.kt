package com.henrystudio.moneymanager.ui.bookmark

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.model.AppDatabase
import com.henrystudio.moneymanager.model.Transaction
import com.henrystudio.moneymanager.ui.search.TransactionAdapter
import com.henrystudio.moneymanager.viewmodel.TransactionViewModel
import com.henrystudio.moneymanager.viewmodel.TransactionViewModelFactory

class AddBookmarkFragment : Fragment() {
    private lateinit var viewModel: TransactionViewModel
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvNoData: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_bookmark, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvNoData = view.findViewById(R.id.add_bookmark_noData)
        recyclerView = view.findViewById(R.id.add_bookmarkRecyclerView)
        val dao = AppDatabase.getDatabase(requireActivity().application).transactionDao()
        val factory = TransactionViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]

        transactionAdapter = TransactionAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = transactionAdapter

        viewModel.allTransactions.observe(viewLifecycleOwner) { list ->
            tvNoData.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            transactionAdapter.updateList(list)
        }

        transactionAdapter.clickListener = object : TransactionAdapter.OnTransactionClickListener{
            override fun onTransactionClick(transaction: Transaction): Boolean {
                val updated = transaction.copy(isBookmarked = true)
                viewModel.update(updated)
                requireActivity().supportFragmentManager.popBackStack()
                return true
            }
        }
    }
}