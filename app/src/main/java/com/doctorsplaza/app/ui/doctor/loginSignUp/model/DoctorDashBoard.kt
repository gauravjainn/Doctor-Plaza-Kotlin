package com.doctorsplaza.app.ui.doctor.loginSignUp.model

data class DoctorDashBoard(
    val cancel_appointments: Int,
    val status: Int,
    val message: String,
    val revenue: Int,
    val clinic: Int,
    val success: Boolean,
    val total_appointments: Int
)