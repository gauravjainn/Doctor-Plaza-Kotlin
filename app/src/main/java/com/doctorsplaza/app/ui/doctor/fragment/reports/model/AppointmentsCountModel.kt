package com.doctorsplaza.app.ui.doctor.fragment.reports.model

data class AppointmentsCountModel(

    val weekrescount: Int,
    val monthrescount: Int,
    val yearrescount: Int,

    val totalamount: Int,
    val onlinepayment: Int,
    val cashpayment: Int,

    val weekcancelcount: Int,
    val monthcancelcount: Int,
    val yearcancelcount: Int,


    val weekdonecount: Int,
    val monthdonecount: Int,
    val yeardonecount: Int

)