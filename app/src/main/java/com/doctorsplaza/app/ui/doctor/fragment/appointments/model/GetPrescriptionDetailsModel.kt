package com.doctorsplaza.app.ui.doctor.fragment.appointments.model

import com.doctorsplaza.app.ui.doctor.fragment.prescription.model.Medicine

data class GetPrescriptionDetailsModel(
    val code: Int,
    val data: List<PrescriptionData>,
    val message: String,
    val status: Int,
    val total: Int
)

data class PrescriptionData(
    val _id: String,
    val ailment_diagnosed: String,
    val appointmentId: String,
    val complain: String,
    val createdAt: String,
    val disease: String,
    val doctorId: String,
    val gender: String,
    val image: String,
    val instructions: String,
    val is_active: Boolean,
    val medicine: List<Medicine>,
    val patientId: PatientId,
    val patient_note: String,
    val patientname: String,
    val suggestion: String,
    val type: String,
    val updatedAt: String
)

data class PatientId(
    val _id: String,
    val email: String,
    val gender: String,
    val patient_name: String,
    val profile_picture: String
)
data class Time(
    val afterBreakfast: Int,
    val afterDinner: Int,
    val afterLunch: Int,
    val beforeBreakfast: Int,
    val beforeDinner: Int,
    val beforeLunch: Int
)

data class AppointmentId(
    val _id: String,
    val age: String,
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
    val room_time_slot_id: String,
    val status: String,
    val updatedAt: String,
    val user_id: String
)