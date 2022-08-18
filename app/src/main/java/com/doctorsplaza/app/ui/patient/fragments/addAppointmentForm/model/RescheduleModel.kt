package com.doctorsplaza.app.ui.patient.fragments.addAppointmentForm.model

data class RescheduleModel(
    val code: Int,
    val status: Int,
    val `data`: Data,
    val message: String,
    val result: Result
)



data class Data(
    val rescheduledBy: String,
    val room_time_slot_id: String,
    val status: String
)

data class Result(
    val _id: String,
    val age: String,
    val appointment_type: String,
    val booked_as: String,
    val cancelledBy: String,
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
    val prescription: Boolean,
    val problem: String,
    val room_time_slot_id: String,
    val status: String,
    val updatedAt: String,
    val user_id: String
)