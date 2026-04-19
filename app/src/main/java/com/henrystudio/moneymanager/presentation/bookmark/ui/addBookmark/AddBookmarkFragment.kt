package com.henrystudio.moneymanager.presentation.bookmark.ui.addBookmark

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.data.model.Transaction
import com.henrystudio.moneymanager.presentation.extension.handle
import com.henrystudio.moneymanager.presentation.viewmodel.SharedTransactionViewModel
import com.henrystudio.moneymanager.presentation.model.UiState
import com.henrystudio.moneymanager.presentation.search.TransactionAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddBookmarkFragment : Fragment() {
    private val sharedViewModel: SharedTransactionViewModel by activityViewModels()
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
                combine(
                    sharedViewModel.allTransactionsState,
                    sharedViewModel.categoriesState
                ) {txState, categories ->
                    Pair(txState, categories)
                }.collect { (txState, categories) ->
                    viewModel.setState(txState)
                    transactionAdapter.setCategories(categories)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    uiState.state.handle(
                        onLoading = {
                            tvNoData.visibility = View.GONE
                            recyclerView.visibility = View.GONE
                        },
                        onEmpty = {
                            tvNoData.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                        },
                        onError = { message ->
                            tvNoData.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                            tvNoData.text = message
                        },
                        onSuccess = { transactions ->
                            tvNoData.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                            transactionAdapter.updateList(transactions)
                        }
                    )
                }

            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.event.collect { event ->
                    when (event) {
                        is AddBookmarkEvent.NavigationBack -> {
                            requireActivity().supportFragmentManager.popBackStack()
                        }
                    }
                }
            }
        }

        transactionAdapter.clickListener = object : TransactionAdapter.OnTransactionClickListener{
            override fun onTransactionClick(transaction: Transaction): Boolean {
                viewModel.onAction(AddBookmarkAction.BookmarkClicked(transaction))
                return true
            }
        }
    }
}