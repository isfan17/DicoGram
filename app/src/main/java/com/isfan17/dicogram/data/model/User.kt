package com.isfan17.dicogram.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val name: String,
    val email: String,
    val userId: String,
    val token: String
): Parcelable
