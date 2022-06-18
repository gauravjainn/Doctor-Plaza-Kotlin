package com.doctorsplaza.app.ui.patient.fragments.addAppointmentForm.model

data class GetDoctorsByClinicDepartment(
    val count: Int,
    val `data`: List<DoctorsData>,
    val status: Int
)

data class DoctorsData(
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
    val name: String,
    val pincode: String,
    val profile_picture: String,
    val promoted: Boolean,
    val qualification: String,
    val rating: Double,
    val ratings_count: Int,
    val searchtype: String,
    val specialization: String,
    val state: String,
    val turndayoff: Boolean,
    val updatedAt: String
){
    override fun toString(): String {
        return doctorName
    }
}