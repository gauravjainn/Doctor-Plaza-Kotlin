package com.doctorsplaza.app.ui.doctor.fragment.home.model

data class  DoctorUpcomingAppointmentModel(
    val cancel_data: Int,
    val data: List<AppointmentData>,
    val message: String,
    val success: Boolean,
    val total: Int
)

data class AppointmentData(
    val _id: String,
    val age: Int,
    val appointment_type: String,
    val booked_as: String,
    val clinic_id: String,
    val consultation_fee: Int,
    val createdAt: String,
    val date: String,
    val departmentname: String,
    val doctor_id: String,
    val doctorname: String,
    val gender: String,
    val mobile: String,
    val mode_of_payment: String,
    val patient_id: String,
    val patientname: String,
    val payment_id: String,
    val payment_status: Boolean,
    val prescription: String,
    val problem: String,
    val room_time_slot_id: RoomTimeSlotId,
    val status: String,
    val updatedAt: String,
    val user_id: UserId
)

data class UserId(
    val _id: String,
    val blood_group: String,
    val contact_number: Long,
    val email: String,
    val gender: String,
    val patient_id: String,
    val patient_name: String,
    val profile_picture: String
)

data class RoomTimeSlotId(
    val _id: String,
    val clinicData: String,
    val createdAt: String,
    val day: String,
    val end_time: String,
    val is_active: Boolean,
    val session_id: String,
    val session_type: String,
    val start_time: String,
    val updatedAt: String
)