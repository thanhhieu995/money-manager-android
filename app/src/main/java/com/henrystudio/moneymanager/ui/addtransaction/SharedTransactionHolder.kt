package com.henrystudio.moneymanager.ui.addtransaction

import com.henrystudio.moneymanager.model.FilterOption

object SharedTransactionHolder {
    var currentFilterDate: String? = null
    var filterOption: FilterOption? = null
    var scrollToAddedTransaction: Boolean = false
    var navigateFromMonthly: Boolean = false  // 🟡 thêm flag này
}