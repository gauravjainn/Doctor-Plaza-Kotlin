package com.doctorsplaza.app.ui.doctor.fragment.reports.model

data class FinancialModel(
    val cashpayment: Int,
    val monthcancelcount: Int,
    val monthdonecount: Int,
    val monthrescount: Int,
    val onlinepayment: Int,
    val totalamount: Int,
    val weekcancelcount: Int,
    val weekdonecount: Int,
    val weekrescount: Int,
    val yearcancelcount: Int,
    val yeardonecount: Int,
    val yearrescount: Int
)