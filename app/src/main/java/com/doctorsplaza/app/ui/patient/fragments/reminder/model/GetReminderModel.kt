package com.doctorsplaza.app.ui.patient.fragments.reminder.model

data class GetReminderModel(
    val count: Int,
    val `data`: List<ReminderData>,
    val status: Int
)

data class ReminderData(
    val _id: String,
    val createdAt: String,
    val date: String,
    val dudate: Dudate,
    val dutime: String,
    val is_active: Boolean,
    val title: String,
    val type: String,
    val updatedAt: String,
    val userId: String
)