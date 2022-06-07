package com.doctorsplaza.app.ui.patient.fragments.search.model

data class SearchModel(
    val code: Int,
    val data: List<SearchData>,
    val message: String,
    val total: Int
)

data class SearchData(
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
    val is_active: Boolean,
    val is_approved: Boolean,
    val is_verified: Boolean,
    val name: String,
    val not_approved: Boolean,
    val password: String,
    val pincode: String,
    val profile_picture: String,
    val promoted: Boolean,
    val qualification: List<String>,
    val rating: Double,
    val ratings_count: Int,
    val roomId: String,
    val searchtype: String,
    val signinotp: String,
    val signupotp: String,
    val specialization: String,
    val state: String,
    val token: Any,
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