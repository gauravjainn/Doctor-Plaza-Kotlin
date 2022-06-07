package com.doctorsplaza.app.data

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import javax.inject.Inject


class Repository @Inject constructor() {
    val service = RetrofitInstance.api

    suspend fun getPatientBanner(jsonBody: JsonObject) = service.getPatientBanner(jsonBody)

    suspend fun getOurSpecialists() = service.getOurSpecialists()

    suspend fun getOurDoctors(pageNo:String,count:String) = service.getOurDoctors(pageNo,count)

    suspend fun checkEmailValidation(json: JsonObject) = service.checkEmailValidation(json)

    suspend fun patientRegistration(json: JsonObject) = service.registerPatient(json)

    suspend fun verifyOTP(json: JsonObject) = service.verifyOTP(json)

    suspend fun verifyLoginOTP(json: JsonObject) = service.verifyOtpLogin(json)

    suspend fun login(json: JsonObject) = service.login(json)

    suspend fun getClinics() = service.getClinicList()

    suspend fun getClinicDoctors(json: JsonObject) = service.getClinicDoctors(json)

    suspend fun getDoctor(doctorId: String) = service.getDoctorDetails(doctorId)

    suspend fun updateProfile(json: JsonObject) = service.updateProfile(json)

    suspend fun refreshToken(json: JsonObject) = service.refreshToken(json)

    suspend fun getProfile(id: String) = service.getProfile(id)

    suspend fun addReport(
        params: MutableMap<String, RequestBody>,
        reportFile: MultipartBody.Part?
    ) = service.addReport(params, reportFile)

    suspend fun getPatientsReport(id: String) = service.getPatientReport(id)

    suspend fun deleteReport(id: String) = service.deleteReport(id)

    suspend fun getUpComingAppointments(patientId: String,pageNo: String) = service.upComingAppointment(patientId, pageNo)

    suspend fun getAppointments(patientId: String,pageNo: String,limit:String,type:String) = service.appointmentsNew(patientId, pageNo,limit,type)

    suspend fun getAppointmentClinics() = service.getAppointmentClinics()

    suspend fun getClinicSpecializations(json: JsonObject) = service.getClinicSpecializations(json)

    suspend fun getDoctorListByClinic(json: JsonObject) = service.getDoctorListByClinic(json)

    suspend fun getRoomSlotDetailsByDrAndClinicId(appointmentType:String,json: JsonObject) = service.getRoomSlotDetailsByDrAndClinicId(appointmentType,json)

    suspend fun updateReminder(reminderId: String, json: JsonObject) =
        service.updateReminder(reminderId, json)

    suspend fun addReminder(json: JsonObject) = service.addReminder(jsonBody = json)

    suspend fun getReminder(patientId: String, date: String) =
        service.getReminder(patientId = patientId, date = date)

    suspend fun getPatientNotification(json: JsonObject) = service.getPatientNotification(json)

    suspend fun getSlugs() = service.getSlugs()

    suspend fun deletePatientAccount(patientId: String, jsonObject: JsonObject) =
        service.deletePatientAccount(patientId)

    suspend fun contactUs(jsonObject: JsonObject) = service.contactUs(jsonBody = jsonObject)

    suspend fun uploadPatientImage(
        patientId: String,
        reportFile: MultipartBody.Part?
    ) = service.patientImageUpload(patientId, reportFile)

    suspend fun bookAppointment(json: JsonObject) = service.bookAppointment(json)

    suspend fun createOrderId(jsonBody: JsonObject) = service.createOrderId(jsonBody)

    suspend fun verifyPayment(jsonBody: JsonObject) = service.verifyPayment(jsonBody)

    suspend fun savePayment(jsonBody: JsonObject) = service.savePayment(jsonBody)

    suspend fun bookingAppointment(jsonBody: JsonObject) = service.bookingAppointment(jsonBody)

    suspend fun cancelAppointment(appointmentId:String,jsonBody: JsonObject) = service.cancelAppointment(appointmentId,jsonBody)

    suspend fun reviewAppointment(jsonBody: JsonObject) = service.reviewAppointment(jsonBody)


    suspend fun getAppointmentDetails(jsonBody: JsonObject) = service.getAppointmentDetails(jsonBody)

    suspend fun rescheduleAppointment(appointmentId:String,jsonBody: JsonObject) = service.rescheduleAppointment(appointmentId,jsonBody)

    suspend fun search(search:String) = service.search(search)

}