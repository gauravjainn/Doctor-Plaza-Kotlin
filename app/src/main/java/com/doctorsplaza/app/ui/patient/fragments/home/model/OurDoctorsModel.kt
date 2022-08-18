package com.doctorsplaza.app.ui.patient.fragments.home.model

import com.doctorsplaza.app.ui.patient.fragments.clinicDoctors.model.DoctorData

data class OurDoctorsModel(
    val count: Int,
    val data: List<DoctorData>,
    val status: Int,
    val message: String,
    val code: Int,
    val total: Int
)
data class OurDoctorData(
    val _id: String,
    val about: String,
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
    val average: Any,
    val is_verified: Boolean,
    val signupotp: String,
    val name: String,
    val pincode: String,
    val profile_picture: String,
    val promoted: Boolean,
    val searchtype: String,
    val signature: String,
    val signinotp: String,
    val specialization: String,
    val state: String,
    val token: String,
    val turndayoff: Boolean,
    val updatedAt: String,
    val rating: String,
    val ratings_count: String,
    val qualification: String,

)
