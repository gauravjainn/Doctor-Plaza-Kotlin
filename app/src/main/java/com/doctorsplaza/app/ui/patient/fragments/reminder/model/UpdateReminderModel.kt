package com.doctorsplaza.app.ui.patient.fragments.reminder.model

data class UpdateReminderModel(
    val data: ReminderData,
    val status: Int,
    val message: String,
    val success: Boolean
)