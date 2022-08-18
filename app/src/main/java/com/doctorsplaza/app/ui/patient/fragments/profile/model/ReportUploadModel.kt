package com.doctorsplaza.app.ui.patient.fragments.profile.model

data class ReportUploadModel(
    val code: Int,
    val status: Int,
    val data: UploadedReportData,
    val message: String
)

data class UploadedReportData(
    val _id: String,
    val createdAt: String,
    val file_name: String,
    val image: String,
    val patient_id: String,
    val permmision: List<Any>,
    val title: String,
    val updatedAt: String
)