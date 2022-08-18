package com.doctorsplaza.app.ui.patient.loginSignUp.model

data class VerificationModel(
    val `data`: VerificationData,
    val message: String,
    val success: Boolean
)

data class VerificationData(
    val _id: String,
    val address: String,
    val blood_group: String,
    val contact_number: Long,
    val createdAt: String,
    val dob: String?="",
    val age: String?="",
    val email: String,
    val gender: String,
    val is_active: Boolean,
    val password: String,
    val patient_id: String,
    val patient_name: String,
    val signinotp: String,
    val signup_details: Boolean,
    val signupotp: String,
    val token: String,
    val updatedAt: String,
    val auth_token: String,
)