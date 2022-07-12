package com.doctorsplaza.app.ui.doctor.fragment.appointments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doctorsplaza.app.data.Repository
import com.doctorsplaza.app.data.commonModel.CommonModel
import com.doctorsplaza.app.ui.doctor.fragment.appointments.model.GetPrescriptionDetailsModel
import com.doctorsplaza.app.ui.doctor.fragment.appointments.model.PrescriptionPDFModel
import com.doctorsplaza.app.ui.patient.fragments.appointments.model.AppointmentModel
import com.doctorsplaza.app.ui.videoCall.model.VideoTokenModel
import com.doctorsplaza.app.utils.Resource
import com.doctorsplaza.app.utils.SessionManager
import com.doctorsplaza.app.utils.getRequestBodyFromFile
import com.google.gson.JsonObject
import com.gym.gymapp.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.IOException
import javax.inject.Inject


@HiltViewModel
class DoctorAppointmentViewModel @Inject constructor(
    private val repository: Repository,
    private val session: SessionManager
) : ViewModel() {

    val doctorAppointmentDetails = SingleLiveEvent<Resource<AppointmentModel>>()
    val doctorAppointmentPrescription = SingleLiveEvent<Resource<GetPrescriptionDetailsModel>>()
    val updateAppointmentStatus = SingleLiveEvent<Resource<CommonModel>>()
    val uploadPrescription = SingleLiveEvent<Resource<CommonModel>>()

    val getPrescriptionPdfUrl = SingleLiveEvent<Resource<PrescriptionPDFModel>>()
    val generateVideoToken = SingleLiveEvent<Resource<VideoTokenModel>>()
    val callNotify = SingleLiveEvent<Resource<CommonModel>>()

    fun getAppointmentDetails(jsonObject: JsonObject) = viewModelScope.launch {
        safeGetAppointmentDetailsCall(jsonObject)
    }

    private suspend fun safeGetAppointmentDetailsCall(jsonObject: JsonObject) {
        doctorAppointmentDetails.postValue(Resource.Loading())
        try {
            val response = repository.getDoctorAppointmentDetails(jsonObject)
            if (response.isSuccessful) {
                response.body()?.let { stateResponse ->
                    doctorAppointmentDetails.postValue(Resource.Success(stateResponse))
                }
            } else {
                doctorAppointmentDetails.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> doctorAppointmentDetails.postValue(
                    Resource.Error(
                        "Network Failure",
                        null
                    )
                )
                else -> doctorAppointmentDetails.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

    fun getAppointmentPrescriptionDetails(id: String) = viewModelScope.launch {
        safeGetAppointmentPrescriptionDetailsCall(id)
    }

    private suspend fun safeGetAppointmentPrescriptionDetailsCall(id: String) {
        doctorAppointmentPrescription.postValue(Resource.Loading())
        try {
            val response = repository.getAppointmentPrescription(id)
            if (response.isSuccessful) {
                response.body()?.let { stateResponse ->
                    doctorAppointmentPrescription.postValue(Resource.Success(stateResponse))
                }
            } else {
                doctorAppointmentPrescription.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> doctorAppointmentPrescription.postValue(
                    Resource.Error(
                        "Network Failure",
                        null
                    )
                )
                else -> doctorAppointmentPrescription.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

    fun updateAppointmentStatus(jsonObject: JsonObject) = viewModelScope.launch {
        safeUdpateAppointmentStatusCall(jsonObject)
    }

    private suspend fun safeUdpateAppointmentStatusCall(jsonObject: JsonObject) {
        updateAppointmentStatus.postValue(Resource.Loading())
        try {
            val response = repository.updateAppointmentStatus(jsonObject)
            if (response.isSuccessful) {
                response.body()?.let { stateResponse ->
                    updateAppointmentStatus.postValue(Resource.Success(stateResponse))
                }
            } else {
                updateAppointmentStatus.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> updateAppointmentStatus.postValue(
                    Resource.Error(
                        "Network Failure",
                        null
                    )
                )
                else -> updateAppointmentStatus.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }


    fun uploadPrescription(file: File, appointmentId:String) = viewModelScope.launch {
        val prescriptionImage = if (file.path.isEmpty()) {
            getRequestBodyFromFile("file", null)
        } else {
            getRequestBodyFromFile("file", file)
        }

        val requestBodyAppointmentId: RequestBody = RequestBody.create(
            "text/plain".toMediaTypeOrNull(),
            appointmentId
        )


        safeUploadPrescriptionCall(prescriptionImage,requestBodyAppointmentId)
    }

    private suspend fun safeUploadPrescriptionCall(
        prescriptionImage: MultipartBody.Part?,
        requestBodyAppointmentId: RequestBody,

        ) {
        uploadPrescription.postValue(Resource.Loading())
        try {
            val response = repository.uploadPrescription(requestBodyAppointmentId,prescriptionImage)
            if (response.isSuccessful) {
                response.body()?.let { stateResponse ->
                    uploadPrescription.postValue(Resource.Success(stateResponse))
                }
            } else {
                uploadPrescription.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> uploadPrescription.postValue(
                    Resource.Error(
                        "Network Failure",
                        null
                    )
                )
                else -> uploadPrescription.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

    fun getPrescriptionPdfUrl(prescriptionId:String) = viewModelScope.launch {
        safeGetPrescriptionPdfUrlCall(prescriptionId)
    }

    private suspend fun safeGetPrescriptionPdfUrlCall(
        prescriptionId: String
    ) {
        getPrescriptionPdfUrl.postValue(Resource.Loading())
        try {
            val response = repository.getPrescriptionUrl(prescriptionId)
            if (response.isSuccessful) {
                response.body()?.let { stateResponse ->
                    getPrescriptionPdfUrl.postValue(Resource.Success(stateResponse))
                }
            } else {
                getPrescriptionPdfUrl.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> getPrescriptionPdfUrl.postValue(
                    Resource.Error(
                        "Network Failure",
                        null
                    )
                )
                else -> getPrescriptionPdfUrl.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

    fun generateVideoToken(jsonObject: JsonObject) = viewModelScope.launch {
        safeGenerateVideoTokenCall(jsonObject)
    }

    private suspend fun safeGenerateVideoTokenCall(jsonObject: JsonObject) {
        generateVideoToken.postValue(Resource.Loading())
        try {
            val response = repository.generateVideoToken(jsonObject)
            if (response.isSuccessful) {
                response.body()?.let { stateResponse ->
                    generateVideoToken.postValue(Resource.Success(stateResponse))
                }
            } else {
                generateVideoToken.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> generateVideoToken.postValue(
                    Resource.Error(
                        "Network Failure",
                        null
                    )
                )
                else -> generateVideoToken.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

    fun callNotify(jsonObject: JsonObject) = viewModelScope.launch {
        safeCallNotifyCall(jsonObject)
    }

    private suspend fun safeCallNotifyCall(jsonObject: JsonObject) {
        callNotify.postValue(Resource.Loading())
        try {
            val response = repository.callNotify(jsonObject)
            if (response.isSuccessful) {
                response.body()?.let { stateResponse ->
                    callNotify.postValue(Resource.Success(stateResponse))
                }
            } else {
                callNotify.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> callNotify.postValue(
                    Resource.Error(
                        "Network Failure",
                        null
                    )
                )
                else -> callNotify.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }
}