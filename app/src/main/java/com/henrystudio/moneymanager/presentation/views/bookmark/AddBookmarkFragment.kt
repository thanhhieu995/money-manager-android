package com.henrystudio.moneymanager.presentation.bookmark

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.viewmodel.AddBookmarkViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.SharedTransactionViewModel
import com.henrystudio.moneymanager.presentation.views.daily.DataTransactionGroupState
import com.henrystudio.moneymanager.presentation.views.search.TransactionAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddBookmarkFragment : Fragment() {
    private val sharedViewModel: SharedTransactionViewModel by viewModels()
    private val viewModel: AddBookmarkViewModel by viewModels()
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvNoData = view.findViewById(R.id.add_bookmark_noData)
        recyclerView = view.findViewById(R.id.add_bookmarkRecyclerView)

        transactionAdapter = TransactionAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = transactionAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.allTransactionsState.collect { state ->
                    when (state) {
                        is DataTransactionGroupState.Loading -> {
                            viewModel.setLoading()
                        }
                        is DataTransactionGroupState.Empty -> {
                            viewModel.setEmpty()
                        }
                        is DataTransactionGroupState.Success -> {
                            viewModel.updateTransactions(state.data)
                        }
                        else -> {}
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    transactionAdapter.updateList(state.transactions)
                }
            }
        }

        transactionAdapter.clickListener = object : TransactionAdapter.OnTransactionClickListener{
            override fun onTransactionClick(transaction: Transaction): Boolean {
                val updated = transaction.copy(isBookmarked = true)
                sharedViewModel.update(updated)
                requireActivity().supportFragmentManager.popBackStack()
                return true
            }
        }
    }
}