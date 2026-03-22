package com.henrystudio.moneymanager.presentation.views.addtransaction

import com.henrystudio.moneymanager.presentation.model.ItemType

sealed class AddTransactionEvent {
    data class NavigateToAddItem(val action: AddItemAction, val itemType: ItemType): AddTransactionEvent()
    data class NavigateToEditItem(val action: AddItemAction): AddTransactionEvent()
    data class NavigateToEditAccount(val action: AddItemAction): AddTransactionEvent()
    data class NavigateToCategoryDetail(val item: EditItem, val action: AddItemAction): AddTransactionEvent()
    object NavigateBackToDaily: AddTransactionEvent()
    object PopBack: AddTransactionEvent()
}