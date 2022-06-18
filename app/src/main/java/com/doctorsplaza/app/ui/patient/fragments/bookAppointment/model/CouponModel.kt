package com.doctorsplaza.app.ui.patient.fragments.bookAppointment.model

data class CouponModel(
    val code: Int,
    val message: String,
    val data: CouponData
)

data class CouponData(
    val discounted_price: Int,
    val final_price: Int,
    val original_price: Int
)