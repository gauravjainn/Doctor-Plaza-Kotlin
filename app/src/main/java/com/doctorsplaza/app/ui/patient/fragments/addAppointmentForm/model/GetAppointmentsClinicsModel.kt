package com.doctorsplaza.app.ui.patient.fragments.addAppointmentForm.model

data class GetAppointmentsClinicsModel(
    val code: Int,
    val status: Int,
    val message: String,
    val success: Boolean,
    val data: List<ClinicsData>
)

data class ClinicsData(
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
){
    override fun toString(): String {
        return clinicName
    }
}

data class Coords(
    val coordinates: List<Double>,
    val type: String
)