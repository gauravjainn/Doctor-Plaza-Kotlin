package com.doctorsplaza.app.ui.patient.fragments.appointments.model

import java.io.Serializable


data class AppointmentModel(
    val data: AppointmentData,
    val message: String,
    val status: Int,
    val success: Boolean
)

data class AppointmentData(
    val _id: String,
    val age: String,
    val appointment_type: String,
    val booked_as: String,
    val clinic_id: ClinicId,
    val consultation_fee: Int,
    val createdAt: String,
    val date: String,
    val departmentname: String,
    val doctor_id: DoctorId,
    val doctorname: String,
    val gender: String,
    val mobile: String,
    val mode_of_payment: String,
    val patient_id: String,
    val patientname: String,
    val payment_id: PaymentId,
    val payment_status: Boolean,
    val problem: String?="",
    val room_time_slot_id: RoomTimeSlotId,
    val status: String,
    val updatedAt: String,
    val user_id: UserId,
    val rating:Rating?,
    val prescription:String,
    val pat_reports:List<PatientReports>

) : Serializable

data class PatientReports(
 val _id:String,
 val title:String,
 val image:String
)
data class PaymentId(
    val _id: String,
    val amount: Int,
    val createdAt: String,
    val doctor_id: String,
    val order_id: String,
    val payment_id: String,
    val payment_status: String,
    val updatedAt: String,
    val user_id: String
)

data class ClinicId(
    val _id: String,
    val city: String,
    val clinicContactNumber: Long,
    val clinicManagerId: String,
    val clinicName: String,
    val comment: String,
    val coords: Coords,
    val createdAt: String,
    val end_time: String,
    val floorCount: String,
    val image: String,
    val location: String,
    val name: String,
    val pincode: String,
    val searchtype: String,
    val start_time: String,
    val state: String,
    val status: Boolean,
    val updatedAt: String,
    val vicinity: String
)


data class DoctorId(
    val _id: String,
    val address: String,
    val city: String,
    val clinicData: String,
    val consultationfee: String,
    val contactNumber: Long,
    val createdAt: String,
    val doctorName: String,
    val doctor_id: String,
    val email: String,
    val is_active: Boolean,
    val is_approved: Boolean,
    val is_verified: Boolean,
    val name: String,
    val not_approved: Boolean,
    val password: String,
    val pincode: String,
    val profile_picture: String,
    val promoted: Boolean,
    val qualification: String,
    val rating: Double,
    val ratings_count: Int,
    val roomId: String,
    val searchtype: String,
    val signinotp: String,
    val signupotp: String,
    val specialization: String,
    val state: String,
    val token: Any,
    val turndayoff: Boolean,
    val updatedAt: String
)

data class RoomTimeSlotId(
    val _id: String,
    val clinicData: String,
    val createdAt: String,
    val date: String,
    val day: String,
    val doctorData: String,
    val is_active: Boolean,
    val roomData: String,
    val timeSlotData: TimeSlotData,
    val updatedAt: String
)

data class TimeSlotData(
    val _id: String,
    val session_id: String,
    val session_type: String,
    val clinicData: String,
    val day: String,
    val start_time: String,
    val end_time: String,
    val startTimeDate:String,
    val endTimeDate:String,
    val is_active: Boolean,
    val createdAt: String,
    val updatedAt: String
)

data class Coords(
    val coordinates: List<Double>,
    val type: String
)

data class UserId(
    val _id: String,
    val address: String,
    val blood_group: String,
    val contact_number: Long,
    val createdAt: String,
    val dob: Any,
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
    val updatedAt: String,

)

data class Rating(
    val _id:String,
    val rating:Any,
    val review:String,
)