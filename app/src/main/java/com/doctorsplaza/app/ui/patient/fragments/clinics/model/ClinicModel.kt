package com.doctorsplaza.app.ui.patient.fragments.clinics.model

data class ClinicModel(
    val count: Int,
    val `data`: List<ClinicData>,
    val message: String,
    val status: Int
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
    val image: String,
    val start_time: String,
    val end_time: String,
    val floorCount: String,
    val location: String,
    val name: String,
    val pincode: String,
    val searchtype: String,
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
    val staff_id: String,
    val user_name: String,
    val email: String,
    val employee_id: String,
)