package com.doctorsplaza.app.ui.doctor.fragment.reports.model

data class RentStatusModel(
    val data: List<RentData>,
    val message: String,
    val success: Boolean
)

data class RentData(
    val _id: String,
    val clinicData: ClinicData,
    val createdAt: String,
    val doctorData: String,
    val is_active: Boolean,
    val month: String,
    val rentPerMonth: Int,
    val rent_status: String,
    val updatedAt: String
)

data class Coords(
    val coordinates: List<Double>,
    val type: String
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