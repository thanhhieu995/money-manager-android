package com.henrystudio.moneymanager.presentation.views.addtransaction

import com.henrystudio.moneymanager.presentation.model.FilterOption

object SharedTransactionHolder {
    var currentFilterDate: String? = null
    var filterOption: FilterOption? = null
    var scrollToAddedTransaction: Boolean = false
    var navigateFromMonthly: Boolean = false  // 🟡 thêm flag này
}