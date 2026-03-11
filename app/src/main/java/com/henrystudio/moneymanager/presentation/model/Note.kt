package com.henrystudio.moneymanager.presentation.model

import java.io.Serializable

data class Note(
 val note: String,
 val count: Int,
 val amount: Double
) : Serializable
