package com.doctorsplaza.app.ui.patient.fragments.profile.model

data class GetPatientReportsModel(
    val code: Int,
    val data: List<PatientReportData>,
    val message: String,
    val success: Boolean,
    val total: Int
)

data class PatientReportData(
    val _id: String,
    val createdAt: String,
    val file_name: String,
    val image: String,
    val patient_id: String,
    val permmision: List<Any>,
    val title: String,
    val updatedAt: String
)