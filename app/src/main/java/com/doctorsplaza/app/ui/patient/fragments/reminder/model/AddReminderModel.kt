package com.doctorsplaza.app.ui.patient.fragments.reminder.model

data class AddReminderModel(
    val data: ReminderData,
    val message: String,
    val result: Result,
    val status: Int
)


data class Result(
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


data class Dudate(
    val dateRange: String,
    val isRange: Boolean,
    val singleDate: SingleDate
)

data class SingleDate(
    val date: String,
    val epoc: String,
    val formatted: String,
    val jsDate: String
)