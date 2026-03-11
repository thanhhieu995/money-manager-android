package com.henrystudio.moneymanager.presentation.views.addtransaction

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.henrystudio.moneymanager.R
import com.henrystudio.moneymanager.data.local.AppDatabase
import com.henrystudio.moneymanager.databinding.FragmentAddItemBinding
import com.henrystudio.moneymanager.core.util.Helper.Companion.toCategory
import com.henrystudio.moneymanager.data.model.Account
import com.henrystudio.moneymanager.data.model.Category
import com.henrystudio.moneymanager.data.model.CategoryType
import com.henrystudio.moneymanager.data.repository.AccountRepositoryImpl
import com.henrystudio.moneymanager.data.repository.CategoryRepositoryImpl
import com.henrystudio.moneymanager.domain.usecase.account.AccountUseCases
import com.henrystudio.moneymanager.domain.usecase.account.AddAccountUseCase
import com.henrystudio.moneymanager.domain.usecase.account.DeleteAccountUseCase
import com.henrystudio.moneymanager.domain.usecase.account.GetAccountsUseCase
import com.henrystudio.moneymanager.domain.usecase.account.UpdateAccountUseCase
import com.henrystudio.moneymanager.domain.usecase.category.CategoryUseCases
import com.henrystudio.moneymanager.domain.usecase.category.DeleteCategoryByIdUseCase
import com.henrystudio.moneymanager.domain.usecase.category.DeleteCategoryUseCase
import com.henrystudio.moneymanager.domain.usecase.category.GetAllCategoriesUseCase
import com.henrystudio.moneymanager.domain.usecase.category.GetCategoriesByTypeUseCase
import com.henrystudio.moneymanager.domain.usecase.category.GetChildCategoriesUseCase
import com.henrystudio.moneymanager.domain.usecase.category.GetParentCategoriesUseCase
import com.henrystudio.moneymanager.domain.usecase.category.InsertCategoryUseCase
import com.henrystudio.moneymanager.domain.usecase.category.UpdateCategoryUseCase
import com.henrystudio.moneymanager.presentation.model.AddItemSource
import com.henrystudio.moneymanager.presentation.model.ItemType
import com.henrystudio.moneymanager.presentation.viewmodel.AccountViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.AccountViewModelFactory
import com.henrystudio.moneymanager.presentation.viewmodel.CategoryViewModel
import com.henrystudio.moneymanager.presentation.viewmodel.CategoryViewModelFactory

class AddItemFragment : Fragment() {

    private var _binding: FragmentAddItemBinding? = null
    private val biding get() = _binding!!
    private lateinit var nameText: TextView
    private lateinit var btnSave: Button
    private lateinit var itemType: ItemType
    private lateinit var categoryType: CategoryType
    private lateinit var source: AddItemSource
    private var categoryUpdate: com.henrystudio.moneymanager.presentation.views.addtransaction.CategoryItem?= null
    private var accountUpdate: EditItem.AccountItem?= null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAddItemBinding.inflate(inflater, container, false)
        return biding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        itemType = arguments?.getSerializable("item_type") as? ItemType ?: ItemType.CATEGORY
        categoryType = arguments?.getSerializable("category_type") as? CategoryType ?: CategoryType.EXPENSE
        source = arguments?.getSerializable("source") as? AddItemSource ?: AddItemSource.FROM_ADD_TRANSACTION
        categoryUpdate = arguments?.getSerializable("category_to_edit") as? CategoryItem
        accountUpdate = arguments?.getSerializable("account_to_edit") as? EditItem.AccountItem

        when {
            categoryUpdate != null -> {
                nameText.text = categoryUpdate!!.name
                btnSave.text = "Update"
                (requireActivity() as AddTransactionActivity).addIcon.visibility = View.GONE
            }
            accountUpdate != null -> {
                nameText.text = accountUpdate!!.item.name
                btnSave.text = "Update"
                (requireActivity() as AddTransactionActivity).addIcon.visibility = View.GONE
            }
        }

        btnSave.setOnClickListener {
            if (nameText.text.trim().isNotEmpty()) {
                (requireActivity() as AddTransactionActivity).updateTransactionTitle(if (categoryType == CategoryType.EXPENSE) {
                    requireContext().getString(R.string.Income)
                } else {
                    requireContext().getString(R.string.Expense)
                })
                (requireActivity() as AddTransactionActivity).updateTitleIncoming(if (itemType == ItemType.CATEGORY) {
                    requireContext().getString(R.string.category)
                } else {
                    requireContext().getString(R.string.account)
                })
                when (source) {
                    AddItemSource.FROM_ADD_TRANSACTION -> {
                        (requireActivity() as AddTransactionActivity).switchToBookmarkIconWithFade()
                        (requireActivity() as AddTransactionActivity).animateTitleToCenter((requireActivity() as AddTransactionActivity).titleCurrent)
                    }
                    AddItemSource.FROM_EDIT_ITEM_CATEGORY_DIALOG -> {
                        (requireActivity() as AddTransactionActivity).switchToAddIconWithFade()
                    }
                    AddItemSource.FROM_DETAIL_CATEGORY -> {}
                    AddItemSource.FROM_EDIT_ITEM_ACCOUNT_DIALOG -> {}
                }
                when (itemType) {
                    ItemType.CATEGORY -> {
                        // Xử lý cho category
                        val dao = AppDatabase.getDatabase(requireActivity().application).categoryDao()
                        val repository = CategoryRepositoryImpl(dao)
                        val useCases = CategoryUseCases(
                            getParentCategories= GetParentCategoriesUseCase(repository),
                        getChildCategories=GetChildCategoriesUseCase(repository),
                         getAllCategories= GetAllCategoriesUseCase(repository),

                         getCategoriesByType= GetCategoriesByTypeUseCase(repository),

                         insertCategory= InsertCategoryUseCase(repository),

                         deleteCategory= DeleteCategoryUseCase(repository),

                         deleteCategoryById= DeleteCategoryByIdUseCase(repository),

                         updateCategory= UpdateCategoryUseCase(repository)

                        )
                        val factory = CategoryViewModelFactory(useCases)
                        val viewModel = ViewModelProvider(this, factory)[CategoryViewModel::class.java]

                        val category = categoryUpdate?.toCategory(categoryType)?.copy(
                            name = nameText.text.toString(),
                            parentId = categoryUpdate!!.parentId,
                            id = categoryUpdate!!.id
                        ) ?: Category(
                            emoji = "",
                            name = nameText.text.toString(),
                            type = categoryType
                        )
                        if (categoryUpdate != null) {
                            viewModel.update(category)
                        } else if (source == AddItemSource.FROM_DETAIL_CATEGORY) {
                            val categoryChoose = (requireActivity() as AddTransactionActivity).selectedCategoryItemForAdd
                            val childCategory = Category(
                                emoji = "",
                                name = nameText.text.toString(),
                                parentId = categoryChoose?.id,
                                type = categoryType
                            )
                            viewModel.insert(childCategory)
                        } else {
                            viewModel.insert(category)
                        }
                        (requireActivity() as AddTransactionActivity).popTitleStackAndAnimateBack()
                        (requireActivity() as AddTransactionActivity).addIcon.visibility = View.VISIBLE
                        parentFragmentManager.popBackStack()
                    }
                    ItemType.ACCOUNT -> {
                        // Xử lý cho account
                        val dao = AppDatabase.getDatabase(requireActivity().application).accountDao()
                        val repository = AccountRepositoryImpl(dao)
                        val accountUseCases = AccountUseCases(
                            addAccountUseCase= AddAccountUseCase(repository),
                            deleteAccountUseCase= DeleteAccountUseCase(repository),
                            getAccountsUseCase= GetAccountsUseCase(repository),
                            updateAccountUseCase= UpdateAccountUseCase(repository)
                        )
                        val factory = AccountViewModelFactory(accountUseCases)
                        val viewModel = ViewModelProvider(this, factory)[AccountViewModel::class.java]
                        val account = accountUpdate?.item?.copy(
                            name = nameText.text.toString()
                        ) ?: Account(name = nameText.text.toString())
                        if (accountUpdate != null) {
                            viewModel.update(account)
                        } else {
                            viewModel.insert(account)
                        }
                        (requireActivity() as AddTransactionActivity).popTitleStackAndAnimateBack()
                        (requireActivity() as AddTransactionActivity).addIcon.visibility = View.VISIBLE
                        parentFragmentManager.popBackStack()
                    }
                }
            }
        }
    }

    private fun init(){
        nameText = biding.fragmentAddItemName
        btnSave = biding.fragmentAddItemBtnSave
    }
}