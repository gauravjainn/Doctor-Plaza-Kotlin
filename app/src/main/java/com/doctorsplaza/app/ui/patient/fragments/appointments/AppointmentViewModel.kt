package com.doctorsplaza.app.ui.patient.fragments.appointments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doctorsplaza.app.data.Repository
import com.doctorsplaza.app.ui.patient.commonModel.AppointmentsModel
import com.doctorsplaza.app.ui.patient.commonModel.CommonModel
import com.doctorsplaza.app.ui.patient.fragments.addAppointmentForm.model.*
import com.doctorsplaza.app.ui.patient.fragments.appointments.model.AppointmentModel

import com.doctorsplaza.app.ui.patient.fragments.clinicDoctors.model.ClinicDoctorsModel
import com.doctorsplaza.app.ui.patient.fragments.doctorDetails.model.DoctorDetailsModel
import com.doctorsplaza.app.utils.Resource
import com.doctorsplaza.app.utils.SessionManager
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.gym.gymapp.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class AppointmentViewModel @Inject constructor(
    private val repository: Repository,
    private val session: SessionManager
) : ViewModel() {

    val doctors = SingleLiveEvent<Resource<ClinicDoctorsModel>>()
    val appointments = SingleLiveEvent<Resource<AppointmentsModel>>()
    val pastAppointments = SingleLiveEvent<Resource<AppointmentsModel>>()
    val appointmentClinics = SingleLiveEvent<Resource<GetAppointmentsClinicsModel>>()
    val clinicSpecialization = SingleLiveEvent<Resource<JsonArray>>()
    val clinicDoctors = SingleLiveEvent<Resource<GetDoctorsByClinicDepartment>>()
    val drClinicTimeSlots = SingleLiveEvent<Resource<ConsultationTimeSlotsModel>>()
    val doctor = SingleLiveEvent<Resource<DoctorDetailsModel>>()

    val bookAppointment = SingleLiveEvent<Resource<CommonModel>>()
    val createOrderId = SingleLiveEvent<Resource<CommonModel>>()
    val verifyPayment = SingleLiveEvent<Resource<CommonModel>>()
    val savePayment = SingleLiveEvent<Resource<PaymentDataModel>>()
    val bookingAppointment = SingleLiveEvent<Resource<CommonModel>>()
    val rescheduleAppointment = SingleLiveEvent<Resource<RescheduleModel>>()
    val cancelAppointment = SingleLiveEvent<Resource<CommonModel>>()

    val appointmentDetails = SingleLiveEvent<Resource<AppointmentModel>>()
    val reviewAppointment = SingleLiveEvent<Resource<CommonModel>>()



    fun getDoctor(doctorId: String) = viewModelScope.launch {
        safeCallOurDoctors(doctorId)
    }

    private suspend fun safeCallOurDoctors(doctorId: String) {
        doctor.postValue(Resource.Loading())
        try {
            val response = repository.getDoctor(doctorId)
            if (response.isSuccessful) {
                response.body()?.let { doctorResponse ->
                    doctor.postValue(Resource.Success(doctorResponse))
                }
            } else {
                doctor.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> doctor.postValue(Resource.Error("Network Failure", null))
                else -> doctor.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

/*
    fun getDoctors(clinicId:String) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("clinicId",clinicId)
        safeGetDoctorsCall(jsonObject)
    }

    private suspend fun safeGetDoctorsCall(jsonObject: JsonObject) {
        doctors.postValue(Resource.Loading())
        try {
            val response = repository.getClinicDoctors(jsonObject)
            if (response.isSuccessful) {
                response.body()?.let { clinicsResponse ->
                    doctors.postValue(Resource.Success(clinicsResponse))
                }
            } else {
                doctors.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> doctors.postValue(Resource.Error("Network Failure", null))
                else -> doctors.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }*/


    fun getAppointments(pageNo: String, type: String) = viewModelScope.launch {
        safeAppointmentsCall(pageNo, type)
    }

    private suspend fun safeAppointmentsCall(pageNo: String, type: String) {
        appointments.postValue(Resource.Loading())
        try {
            val response =
                repository.getAppointments(session.patientId, pageNo, limit = "10", type = type)
            if (response.isSuccessful) {
                response.body()?.let { appointmentsResponse ->
                    appointments.postValue(Resource.Success(appointmentsResponse))
                }
            } else {
                appointments.postValue(Resource.Error(response.message(), null))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> appointments.postValue(Resource.Error("Network Failure", null))
                else -> appointments.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

    fun getPastAppointments(pageNo: String, type: String) = viewModelScope.launch {
        safePastAppointmentsCall(pageNo, type)
    }

    private suspend fun safePastAppointmentsCall(pageNo: String, type: String) {
        pastAppointments.postValue(Resource.Loading())
        try {
            val response =
                repository.getAppointments(session.patientId, pageNo, limit = "10", type = type)
            if (response.isSuccessful) {
                response.body()?.let { appointmentsResponse ->
                    pastAppointments.postValue(Resource.Success(appointmentsResponse))
                }
            } else {
                pastAppointments.postValue(Resource.Error(response.message(), null))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> pastAppointments.postValue(
                    Resource.Error(
                        "Network Failure",
                        null
                    )
                )
                else -> pastAppointments.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }


    fun getAppointmentClinics() = viewModelScope.launch {
        safeGetAppointmentClinicsCall()
    }

    private suspend fun safeGetAppointmentClinicsCall() {
        appointmentClinics.postValue(Resource.Loading())
        try {
            val response = repository.getAppointmentClinics()
            if (response.isSuccessful) {
                response.body()?.let { appointmentsResponse ->
                    appointmentClinics.postValue(Resource.Success(appointmentsResponse))
                }
            } else {
                appointmentClinics.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> appointmentClinics.postValue(
                    Resource.Error(
                        "Network Failure",
                        null
                    )
                )
                else -> appointmentClinics.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

    fun getSpecializationsByClinic(jsonObject: JsonObject) = viewModelScope.launch {
        safeGetSpecializationsByClinicCall(jsonObject = jsonObject)
    }

    private suspend fun safeGetSpecializationsByClinicCall(jsonObject: JsonObject) {
        clinicSpecialization.postValue(Resource.Loading())
        try {
            val response = repository.getClinicSpecializations(json = jsonObject)
            if (response.isSuccessful) {
                response.body()?.let { appointmentsResponse ->
                    clinicSpecialization.postValue(Resource.Success(appointmentsResponse))
                }
            } else {
                clinicSpecialization.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> clinicSpecialization.postValue(
                    Resource.Error(
                        "Network Failure",
                        null
                    )
                )
                else -> clinicSpecialization.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

    fun getDoctorListByClinic(jsonObject: JsonObject) = viewModelScope.launch {
        safeGetDoctorListByClinicCall(jsonObject = jsonObject)
    }

    private suspend fun safeGetDoctorListByClinicCall(jsonObject: JsonObject) {
        clinicDoctors.postValue(Resource.Loading())
        try {
            val response = repository.getDoctorListByClinic(json = jsonObject)
            if (response.isSuccessful) {
                response.body()?.let { appointmentsResponse ->
                    clinicDoctors.postValue(Resource.Success(appointmentsResponse))
                }
            } else {
                clinicDoctors.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> clinicDoctors.postValue(Resource.Error("Network Failure", null))
                else -> clinicDoctors.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

    fun getRoomSlotDetailsByDrAndClinicId(appointmentType: String, jsonObject: JsonObject) =
        viewModelScope.launch {
            safeGetRoomSlotDetailsByDrAndClinicIdCall(
                appointmentType = appointmentType,
                jsonObject = jsonObject
            )
        }

    private suspend fun safeGetRoomSlotDetailsByDrAndClinicIdCall(
        appointmentType: String,
        jsonObject: JsonObject
    ) {
        drClinicTimeSlots.postValue(Resource.Loading())
        try {
            val response = repository.getRoomSlotDetailsByDrAndClinicId(
                appointmentType = appointmentType,
                json = jsonObject
            )
            if (response.isSuccessful) {
                response.body()?.let { appointmentsResponse ->
                    drClinicTimeSlots.postValue(Resource.Success(appointmentsResponse))
                }
            } else {
                drClinicTimeSlots.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> drClinicTimeSlots.postValue(
                    Resource.Error(
                        "Network Failure",
                        null
                    )
                )
                else -> drClinicTimeSlots.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }


    fun bookAppointment(jsonObject: JsonObject) = viewModelScope.launch {
        safeBookAppointmentCall(jsonObject = jsonObject)
    }

    private suspend fun safeBookAppointmentCall(jsonObject: JsonObject) {
        bookAppointment.postValue(Resource.Loading())
        try {
            val response = repository.bookAppointment(json = jsonObject)
            if (response.isSuccessful) {
                response.body()?.let { appointmentsResponse ->
                    bookAppointment.postValue(Resource.Success(appointmentsResponse))
                }
            } else {
                bookAppointment.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> bookAppointment.postValue(Resource.Error("Network Failure", null))
                else -> bookAppointment.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

    fun createOrderId(jsonObject: JsonObject) = viewModelScope.launch {
        safeCreateOrderIdCall(jsonObject)
    }

    private suspend fun safeCreateOrderIdCall(json: JsonObject) {
        createOrderId.postValue(Resource.Loading())
        try {
            val response = repository.createOrderId(json)
            if (response.isSuccessful) {
                response.body()?.let { doctorResponse ->
                    createOrderId.postValue(Resource.Success(doctorResponse))
                }
            } else {
                createOrderId.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> createOrderId.postValue(Resource.Error("Network Failure", null))
                else -> createOrderId.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

    fun verifyPayment(jsonObject: JsonObject) = viewModelScope.launch {
        safeVerifyPaymentCall(jsonObject)
    }

    private suspend fun safeVerifyPaymentCall(json: JsonObject) {
        verifyPayment.postValue(Resource.Loading())
        try {
            val response = repository.verifyPayment(json)
            if (response.isSuccessful) {
                response.body()?.let { doctorResponse ->
                    verifyPayment.postValue(Resource.Success(doctorResponse))
                }
            } else {
                verifyPayment.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> verifyPayment.postValue(Resource.Error("Network Failure", null))
                else -> verifyPayment.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

    fun savePayment(jsonObject: JsonObject) = viewModelScope.launch {
        safeSavePaymentCall(jsonObject)
    }

    private suspend fun safeSavePaymentCall(json: JsonObject) {
        savePayment.postValue(Resource.Loading())
        try {
            val response = repository.savePayment(json)
            if (response.isSuccessful) {
                response.body()?.let { doctorResponse ->
                    savePayment.postValue(Resource.Success(doctorResponse))
                }
            } else {
                savePayment.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> savePayment.postValue(Resource.Error("Network Failure", null))
                else -> savePayment.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

    fun bookingAppointment(jsonObject: JsonObject) = viewModelScope.launch {
        safeBookingAppointmentCall(jsonObject)
    }

    private suspend fun safeBookingAppointmentCall(json: JsonObject) {
        bookingAppointment.postValue(Resource.Loading())
        try {
            val response = repository.bookingAppointment(json)
            if (response.isSuccessful) {
                response.body()?.let { doctorResponse ->
                    bookingAppointment.postValue(Resource.Success(doctorResponse))
                }
            } else {
                bookingAppointment.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> bookingAppointment.postValue(
                    Resource.Error(
                        "Network Failure",
                        null
                    )
                )
                else -> bookingAppointment.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }


    fun getAppointmentDetails(jsonObject: JsonObject) = viewModelScope.launch {
        safeAppointmentDetailsCall(jsonObject)
    }

    private suspend fun safeAppointmentDetailsCall(json: JsonObject) {
        appointmentDetails.postValue(Resource.Loading())
        try {
            val response = repository.getAppointmentDetails(json)
            if (response.isSuccessful) {
                response.body()?.let { doctorResponse ->
                    appointmentDetails.postValue(Resource.Success(doctorResponse))
                }
            } else {
                appointmentDetails.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> appointmentDetails.postValue(
                    Resource.Error(
                        "Network Failure",
                        null
                    )
                )
                else -> appointmentDetails.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

    fun rescheduleAppointment(appointmentId: String, jsonObject: JsonObject) =
        viewModelScope.launch {
            safeRescheduleAppointmentCall(appointmentId, jsonObject)
        }

    private suspend fun safeRescheduleAppointmentCall(appointmentId: String, json: JsonObject) {
        rescheduleAppointment.postValue(Resource.Loading())
        try {
            val response = repository.rescheduleAppointment(appointmentId, json)
            if (response.isSuccessful) {
                response.body()?.let { doctorResponse ->
                    rescheduleAppointment.postValue(Resource.Success(doctorResponse))
                }
            } else {
                rescheduleAppointment.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> rescheduleAppointment.postValue(
                    Resource.Error(
                        "Network Failure",
                        null
                    )
                )
                else -> rescheduleAppointment.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

    fun cancelAppointment(appointmentId: String, jsonObject: JsonObject) = viewModelScope.launch {
        safeCancelAppointmentCall(appointmentId, jsonObject)
    }

    private suspend fun safeCancelAppointmentCall(appointmentId: String, json: JsonObject) {
        cancelAppointment.postValue(Resource.Loading())
        try {
            val response = repository.cancelAppointment(appointmentId, json)
            if (response.isSuccessful) {
                response.body()?.let { doctorResponse ->
                    cancelAppointment.postValue(Resource.Success(doctorResponse))
                }
            } else {
                cancelAppointment.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> cancelAppointment.postValue(
                    Resource.Error(
                        "Network Failure",
                        null
                    )
                )
                else -> cancelAppointment.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

    fun reviewAppointment( jsonObject: JsonObject) = viewModelScope.launch {
        safeReviewAppointmentCall( jsonObject)
    }

    private suspend fun safeReviewAppointmentCall( json: JsonObject) {
        reviewAppointment.postValue(Resource.Loading())
        try {
            val response = repository.reviewAppointment(json)
            if (response.isSuccessful) {
                response.body()?.let { doctorResponse ->
                    reviewAppointment.postValue(Resource.Success(doctorResponse))
                }
            } else {
                reviewAppointment.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> reviewAppointment.postValue(
                    Resource.Error(
                        "Network Failure",
                        null
                    )
                )
                else -> reviewAppointment.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }
}