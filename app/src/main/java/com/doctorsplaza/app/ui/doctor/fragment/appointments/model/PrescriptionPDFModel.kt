package com.doctorsplaza.app.ui.doctor.fragment.appointments.model

data class PrescriptionPDFModel(
    val code: Int,
    val status: Int,
    val message: String,
    val perscription: String
)