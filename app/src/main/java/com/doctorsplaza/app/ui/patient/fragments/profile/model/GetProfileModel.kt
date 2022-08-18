package com.doctorsplaza.app.ui.patient.fragments.profile.model

data class GetProfileModel(
    val data: ProfileData,
    val message: String,
    val status: Int
)

data class ProfileData(
    val _id: String,
    val contact_number: Long,
    val createdAt: String,
    val dob: String?="",
    val email: String,
    val gender: String,
    val is_active: Boolean,
    val password: String,
    val patient_id: String,
    val patient_name: String,
    val signinotp: String,
    val signup_details: Boolean,
    val token: String,
    val profile_picture: String,
    val address: String,
    val age: String,
    val updatedAt: String
)