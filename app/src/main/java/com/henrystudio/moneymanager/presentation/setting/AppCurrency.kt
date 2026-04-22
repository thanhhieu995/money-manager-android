package com.henrystudio.moneymanager.presentation.setting

import java.util.Locale

enum class AppCurrency(
    val code: String,
    val locale: Locale
) {
    VND("VND", Locale("vi", "VN")),
    USD("USD", Locale.US),
    EUR("EUR", Locale.GERMANY),
    JPY("JPY", Locale.JAPAN)
}