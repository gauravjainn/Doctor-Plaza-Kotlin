package com.doctorsplaza.app.ui.patient.fragments.notifications.model

data class NotificationModel(
    val count: Int,
    val `data`: List<NotificationData>,
    val status: Int
)

data class NotificationData(
    val _id: String,
    val content: Content,
    val createdAt: String,
    val dest: String,
    val email: String,
    val is_active: Boolean,
    val is_read: Boolean,
    val name: String,
    val patient_id: String,
    val source: String,
    val type: String,
    val updatedAt: String
)

data class Content(
    val doctorname: String,
    val appointmentid: String,
    val clinicmanagername: String,
    val patname: String,
    val appointmenttype: String,
    val patientname: String,
    val title: String,
    val patid: String,
    val name: String,
    val date: String,
    val docspe: String,
    val amount: String,
    val url: String,
    val clinicmanager: String,
    val month: String,
    val rent: String,
    val nextmonth: String,
    val status: String,
    val clinic: String,
    val resdate: String,
    val appid: String,
    val docspec: String,
    val mode_of_payment: String,
    val problem: String,
    val bookdate: String
)