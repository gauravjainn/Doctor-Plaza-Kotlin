package com.doctorsplaza.app.ui.patient.loginSignUp.model

data class LoginModel(
    val OTP: String,
    val `data`: LoginData,
    val message: String,
    val success: Boolean
)

data class LoginData(
    val _id: String,
    val address: String,
    val blood_group: String,
    val contact_number: Long,
    val createdAt: String,
    val dob: String,
    val email: String,
    val gender: String,
    val is_active: Boolean,
    val patient_id: String,
    val patient_name: String,
    val signup_details: Boolean,
    val signupotp: String,
    val token: String,
    val updatedAt: String
)