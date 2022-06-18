package com.doctorsplaza.app.ui.doctor.fragment.profile.model

data class DoctorProfileModel(
    val data: List<DoctorData>,
    val status: Int
)

data class DoctorData(
    val _id: String,
    val address: String,
    val city: String,
    val clinicData: ClinicData,
    val consultationfee: String,
    val contactNumber: Long,
    val createdAt: String,
    val doctorName: String,
    val doctor_id: String,
    val email: String,
    val gender: String,
    val is_active: Boolean,
    val is_approved: Boolean,
    val is_verified: Boolean,
    val name: String,
    val not_approved: Boolean,
    val pincode: String,
    val profile_picture: String,
    val promoted: Boolean,
    val qualification: String,
    val rating: Double,
    val ratings_count: Int,
    val roomId: String,
    val searchtype: String,
    val signinotp: String,
    val signupotp: String,
    val specialization: String,
    val state: String,
    val token: String,
    val turndayoff: Boolean,
    val updatedAt: String
)

data class ClinicData(
    val clinicAddress: String,
    val clinicContact: Long,
    val clinicId: String,
    val clinicName: String
)