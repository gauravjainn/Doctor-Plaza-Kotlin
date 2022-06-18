package com.doctorsplaza.app.ui.doctor.loginSignUp.model

data class DoctorSignupModel(
    val data: SignUpData,
    val message: String,
    val success: Boolean
)

data class SignUpData(
    val _id: String,
    val address: String,
    val city: String,
    val consultationfee: String,
    val contactNumber: Long,
    val createdAt: String,
    val doctorName: String,
    val doctor_id: String,
    val email: String,
    val is_active: Boolean,
    val is_approved: Boolean,
    val is_verified: Boolean,
    val name: String,
    val password: String,
    val pincode: String,
    val promoted: Boolean,
    val searchtype: String,
    val signupotp: String,
    val specialization: String,
    val state: String,
    val token: String,
    val turndayoff: Boolean,
    val updatedAt: String
)