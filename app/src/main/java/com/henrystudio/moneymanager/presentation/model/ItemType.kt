package com.henrystudio.moneymanager.presentation.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class ItemType : Parcelable{
    CATEGORY , ACCOUNT
}