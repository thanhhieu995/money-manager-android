package com.example.moneymanager.ui.daily

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneymanager.databinding.FragmentDailyBinding
import com.example.moneymanager.helper.FilterTransactions
import com.example.moneymanager.model.AppDatabase
import com.example.moneymanager.ui.main.TransactionGroupAdapter
import com.example.moneymanager.viewmodel.TransactionViewModel
import com.example.moneymanager.viewmodel.TransactionViewModelFactory
import java.time.LocalDate

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DailyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DailyFragment : Fragment() {
    private lateinit var viewModel: TransactionViewModel
    private lateinit var adapter: TransactionGroupAdapter
    private var _binding: FragmentDailyBinding? = null
    private val binding get() = _binding!!
    private val filterTransactions = FilterTransactions()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDailyBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dao = AppDatabase.getDatabase(requireActivity().application).transactionDao()
        val factory = TransactionViewModelFactory(dao)
        viewModel = ViewModelProvider(requireActivity(), factory)[TransactionViewModel::class.java]

        adapter = TransactionGroupAdapter()
        binding.transactionList.layoutManager = LinearLayoutManager(requireContext())
        binding.transactionList.adapter = adapter

        viewModel.groupedTransactions.observe(viewLifecycleOwner) { transactions ->
            // Sắp xếp ngày giảm dần
            val now = LocalDate.now()

            val filteredList = filterTransactions.filterTransactionsByMonth(transactions, now)

            adapter.submitList(filteredList)
            binding.noDataText.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.currentMonthYear.observe(viewLifecycleOwner) { selectedMonth ->
            viewModel.groupedTransactions.value?.let { allTransactions ->
                val filtered = filterTransactions.filterTransactionsByMonth(allTransactions, selectedMonth)
                adapter.submitList(filtered)
                binding.noDataText.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}