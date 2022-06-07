package com.doctorsplaza.app.utils

import androidx.annotation.DrawableRes
import androidx.annotation.IdRes

data class CbnMenuItems(
    @DrawableRes
    val icon: Int,
    @DrawableRes
    val avdIcon: Int,
    @IdRes
    val destinationId: Int = -1,
    val title:String
)