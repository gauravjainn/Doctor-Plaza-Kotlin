package com.doctorsplaza.app.ui.doctor.fragment.clinics.model

data class ClinicsListModel(
    val code: Int,
    val data: List<ClinicData>,
    val message: String,
    val success: Boolean
)

data class ClinicData(
    val _id: String,
    val city: String,
    val clinicContactNumber: Long,
    val clinicManagerId: ClinicManagerId,
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

data class ClinicManagerId(
    val _id: String,
    val clinicId: String,
    val clinic_manager_name: String,
    val createdAt: String,
    val email: String,
    val is_active: Boolean,
    val password: String,
    val updatedAt: String
)