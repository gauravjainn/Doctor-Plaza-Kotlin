package com.doctorsplaza.app.ui.patient.fragments.clinicDoctors.model

data class ClinicDoctorsModel(
    val code: Int,
    val count: Int,
    val status: Int,
    val data: List<ClinicDoctorsData>,
    val message: String,
    val success: Boolean
)


data class ClinicDoctorsData(
    val _id: String,
    val average: Any,
    val clinicData: List<ClinicData>,
    val clinicManagerId: List<Any>,
    val createdAt: String,
    val date: String,
    val day: String,
    val doctorData: List<DoctorData>,
    val is_active: Boolean,
    val isfavorite: Boolean,
    val roomData: String,
    val timeSlotData: String,
    val updatedAt: String
)

data class DoctorData(
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
    val average: Any,
    val is_verified: Boolean,
    val signupotp: String,
    val is_active: Boolean,
    val is_approved: Boolean,
    val name: String,
    val password: String,
    val pincode: String,
    val profile_picture: String,
    val promoted: Boolean,
    val qualification: String,
    val rating: Double,
    val ratings_count: Int,
    val experience: String,
    val searchtype: String,
    val signature: String,
    val signinotp: String,
    val specialization: String,
    val state: String,
    val token: String,
    val turndayoff: Boolean,
    val updatedAt: String
)



data class ClinicData(
    val _id: String,
    val city: String,
    val clinicContactNumber: Long,
    val clinicManagerId: String,
    val clinicName: String,
    val comment: String,
    val coords: Coords,
    val createdAt: String,
    val end_time: String,
    val floorCount: String,
    val image: String,
    val location: String,
    val name: String,
    val pincode: String,
    val searchtype: String,
    val start_time: String,
    val state: String,
    val status: Boolean,
    val updatedAt: String,
    val vicinity: String
)

data class Coords(
    val coordinates: List<Double>,
    val type: String
)
