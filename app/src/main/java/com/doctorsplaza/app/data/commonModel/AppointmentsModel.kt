package com.doctorsplaza.app.data.commonModel

data class AppointmentsModel(
    val data: List<AppointmentData>,
    val status: Int,
    val message: String,
    val total: Int

)

data class AppointmentData(
    val _id: String,
    val age: String,
    val appointment_type: String,
    val booked_as: String,
    val cancelledBy: String,
    val clinic_id: ClinicId,
    val consultation_fee: Int,
    val createdAt: String,
    val date: String,
    val departmentname: String,
    val doctor_id: DoctorId,
    val doctorname: String,
    val gender: String,
    val mobile: String,
    val mode_of_payment: String,
    val patient_id: String,
    val patientname: String,
    val payment_id: String,
    val payment_status: Boolean,
    val prescription: Any,
    val problem: String,
    val room_time_slot_id: RoomTimeSlotId,
    val status: String,
    val updatedAt: String,
    val user_id: String
)

data class ClinicId(
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

data class DoctorId(
    val _id: String,
    val address: String,
    val city: String,
    val clinicData: String,
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
    val qualification: String,
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

data class RoomTimeSlotId(
    val timeSlotData: TimeSlotData
)
data class TimeSlotData(
    val _id: String,
    val clinicData: String,
    val createdAt: String,
    val day: String,
    val doctorData: String,
    val end_time: String,
    val is_active: Boolean,
    val session_id: String,
    val session_type: String,
    val start_time: String,
    val updatedAt: String
)

data class Coords(
    val coordinates: List<Double>,
    val type: String
)