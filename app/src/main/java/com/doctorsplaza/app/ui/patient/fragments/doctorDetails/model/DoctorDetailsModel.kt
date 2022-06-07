package com.doctorsplaza.app.ui.patient.fragments.doctorDetails.model

data class DoctorDetailsModel(
    val data: List<DoctorDetailsData>,
    val status: Int
)

data class DoctorDetailsData(
    val _id: String,
    val about: String,
    val address: String,
    val city: String,
    val clinicData: ClinicData,
    val consultationfee: String,
    val contactNumber: Long,
    val createdAt: String,
    val doctorName: String,
    val doctor_id: String,
    val email: String,
    val is_active: Boolean,
    val is_approved: Boolean,
    val name: String,
    val password: String,
    val pincode: String,
    val profile_picture: String,
    val promoted: Boolean,
    val qualification: List<String>,
    val rating: Double,
    val ratings_count: Int,
    val searchtype: String,
    val signupotp: String,
    val specialization: String,
    val state: String,
    val turndayoff: Boolean,
    val updatedAt: String
)

data class ClinicData(
    val clinicAddress: String,
    val clinicContact: Long,
    val clinicId: String,
    val clinicName: String
)