package com.doctorsplaza.app.ui.patient.fragments.addAppointmentForm.model

data class ConsultationTimeSlotsModel(
    val code: Int,
    val status: Int,
    val message: String,
    val success: Boolean,
    val data: List<RoomTimeSlotsData>,
    val total: Int
)

data class RoomTimeSlotsData(
    val _id: String,
    val clinicData: String,
    val date: String,
    val day: String,
    val doctorData: String,
    val roomData: RoomData,
    val session_type: String,
    val timeSlotData: TimeSlotData
)

data class RoomData(
    val _id: String,
    val clinicData: String,
    val createdAt: String,
    val floor: String,
    val paidStatus: Boolean,
    val rentPerMonth: Int,
    val roomName: String,
    val roomNumber: Int,
    val room_id: String,
    val specialization: String,
    val updatedAt: String
)

data class TimeSlotData(
    val _id: String,
    val end_time: String,
    val start_time: String,
    var isSelected :Boolean = false
)