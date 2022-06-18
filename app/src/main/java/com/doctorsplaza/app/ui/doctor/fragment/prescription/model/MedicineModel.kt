package com.doctorsplaza.app.ui.doctor.fragment.prescription.model

data class MedicineModel(
    val ailmentDiagnosed: String,
    val appointmentId: String,
    val complain: String,
    val date: String,
    val disease: String,
    val instructions: String,
    val medicines: List<Medicine>,
    val patientNote: String,
    val suggestion: String,
    var isExpanded:Boolean = false
)


