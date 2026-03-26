package com.henrystudio.moneymanager.presentation.addtransaction.model

import com.henrystudio.moneymanager.presentation.model.ItemType
import java.time.LocalDate

sealed class AddTransactionEvent {
    data class NavigateToAddItem(val action: AddItemAction, val itemType: ItemType): AddTransactionEvent()
    data class NavigateToEditItem(val itemType: ItemType ,val action: AddItemAction): AddTransactionEvent()
    data class NavigateToEditAccount(val action: AddItemAction): AddTransactionEvent()
    data class NavigateToCategoryDetail(val item: EditItem, val action: AddItemAction): AddTransactionEvent()
    object NavigateBackToDaily: AddTransactionEvent()
    object PopBack: AddTransactionEvent()
    data class FocusField(val fieldType: FieldType?): AddTransactionEvent()
    object CloseScreen : AddTransactionEvent()
    object ResetForm : AddTransactionEvent()
    object NavigateBack: AddTransactionEvent()
    data class ShowToast(val message: String): AddTransactionEvent()
    data class SaveCompleted(val date: String, val localDate: LocalDate?, val closeAfterSave: Boolean): AddTransactionEvent()
}