package com.doctorsplaza.app.ui.doctor.fragment.prescription.model

import java.io.Serializable

data class AddPrescriptionModel(
    val data: AddPrescriptionData,
    val status: Int,
    val message: String,
    val success: Boolean
)

data class AddPrescriptionData(
    val _id: String,
    val ailment_diagnosed: String,
    val appointmentId: String,
    val complain: String,
    val createdAt: String,
    val disease: String,
    val doctorId: String,
    val instructions: String,
    val is_active: Boolean,
    val medicine: List<Medicine>,
    val patientId: PatientId,
    val patient_note: String,
    val patientname: String,
    val suggestion: String,
    val updatedAt: String
)

data class Medicine(
    val days: String,
    val medicineInstruction: String,
    val medicineName: String,
    val medicineType: String,
    val time: List<Time>,
    var isExpanded:Boolean = false
) : Serializable

data class PatientId(
    val _id: String,
    val address: String,
    val blood_group: String,
    val contact_number: Long,
    val createdAt: String,
    val dob: String,
    val email: String,
    val gender: String,
    val is_active: Boolean,
    val password: String,
    val patient_id: String,
    val patient_name: String,
    val profile_picture: String,
    val signinotp: String,
    val signup_details: Boolean,
    val signupotp: String,
    val token: String,
    val updatedAt: String
)
data class Time(
    val afterBreakfast: Int,
    val afterDinner: Int,
    val afterLunch: Int,
    val beforeBreakfast: Int,
    val beforeDinner: Int,
    val beforeLunch: Int
)