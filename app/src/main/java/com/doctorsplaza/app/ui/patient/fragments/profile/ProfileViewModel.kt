package com.doctorsplaza.app.ui.patient.fragments.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doctorsplaza.app.data.Repository
import com.doctorsplaza.app.data.commonModel.CommonModel
import com.doctorsplaza.app.ui.patient.fragments.profile.model.*
import com.doctorsplaza.app.utils.Resource
import com.google.gson.JsonObject
import com.doctorsplaza.app.utils.SessionManager
import com.doctorsplaza.app.utils.getRequestBodyFromFile
import com.gym.gymapp.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val repository: Repository,private val session: SessionManager):ViewModel(){
    val updateProfile = SingleLiveEvent<Resource<UpdateProfileModel>>()
    val profile = SingleLiveEvent<Resource<GetProfileModel>>()
    val addReport = SingleLiveEvent<Resource<ReportUploadModel>>()
    val getPatientReports = SingleLiveEvent<Resource<GetPatientReportsModel>>()
    val deleteReport = SingleLiveEvent<Resource<DeleteReportModel>>()
    val uploadPatientImage = SingleLiveEvent<Resource<ProfileImageUploadModel>>()
    val refreshToken = SingleLiveEvent<Resource<CommonModel>>()



    fun refreshToken(json:  JsonObject) = viewModelScope.launch {
        safeRefreshTokenCall(json)
    }

    private suspend fun safeRefreshTokenCall(json: JsonObject) {
        refreshToken.postValue(Resource.Loading())
        try {
            val response = repository.refreshToken(json)
            if (response.isSuccessful) {
                response.body()?.let { doctorResponse ->
                    refreshToken.postValue(Resource.Success(doctorResponse))
                }
            } else {
                refreshToken.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> refreshToken.postValue(Resource.Error("Network Failure", null))
                else -> refreshToken.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

    fun updateProfile(json:  JsonObject) = viewModelScope.launch {
        safeUpdateProfileCall(json)
    }

    private suspend fun safeUpdateProfileCall(json: JsonObject) {
        updateProfile.postValue(Resource.Loading())
        try {
            val response = repository.updateProfile(json)
            if (response.isSuccessful) {
                response.body()?.let { doctorResponse ->
                    updateProfile.postValue(Resource.Success(doctorResponse))
                }
            } else {
                updateProfile.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> updateProfile.postValue(Resource.Error("Network Failure", null))
                else -> updateProfile.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }


    fun getProfile() = viewModelScope.launch {
        safeGetProfileCall()
    }


    private suspend fun safeGetProfileCall() {
        profile.postValue(Resource.Loading())
        try {
            val response = repository.getProfile(session.patientId)
            if (response.isSuccessful) {
                response.body()?.let { profileResponse ->
                    profile.postValue(Resource.Success(profileResponse))
                }
            } else {
                profile.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> profile.postValue(Resource.Error("Network Failure", null))
                else -> profile.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

    fun addReport(params: MutableMap<String, RequestBody>, file: File) = viewModelScope.launch {
        val reportFile = if (file.path.isEmpty()) {
            getRequestBodyFromFile("file", null)
        } else {
            getRequestBodyFromFile("file", file)
        }

        safeAddReportCall(params,reportFile)
    }


    private suspend fun safeAddReportCall(
        params: MutableMap<String, RequestBody>,
        reportFile: MultipartBody.Part?
    ) {
        addReport.postValue(Resource.Loading())
        try {
            val response = repository.addReport(params,reportFile)
            if (response.isSuccessful) {
                response.body()?.let { profileResponse ->
                    addReport.postValue(Resource.Success(profileResponse))
                }
            } else {
                addReport.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {

                is IOException -> addReport.postValue(Resource.Error("Network Failure ${t.message}", null))
                else -> addReport.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

    fun uploadPatientImage(file: File) = viewModelScope.launch {
        val reportFile = if (file.path.isEmpty()) {
            getRequestBodyFromFile("file", null)
        } else {
            getRequestBodyFromFile("file", file)
        }
        safeUploadPatientImageCall(reportFile)
    }


    private suspend fun safeUploadPatientImageCall(reportFile: MultipartBody.Part?) {
        uploadPatientImage.postValue(Resource.Loading())

        try {
            val response = repository.uploadPatientImage(session.patientId,reportFile)
            if (response.isSuccessful) {
                response.body()?.let { profileResponse ->
                    uploadPatientImage.postValue(Resource.Success(profileResponse))
                }
            } else {
                uploadPatientImage.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> uploadPatientImage.postValue(Resource.Error("Network Failure", null))
                else -> uploadPatientImage.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

    fun getPatientReport() = viewModelScope.launch {
        safeGetPatientReportCall()
    }


    private suspend fun safeGetPatientReportCall() {
        getPatientReports.postValue(Resource.Loading())
        try {
            val response = repository.getPatientsReport(session.patientId)
            if (response.isSuccessful) {
                response.body()?.let { profileResponse ->
                    getPatientReports.postValue(Resource.Success(profileResponse))
                }
            } else {
                getPatientReports.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> getPatientReports.postValue(Resource.Error("Network Failure", null))
                else -> getPatientReports.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

    fun deleteReport(reportId:String) = viewModelScope.launch {
        safeDeleteReportCall(reportId)
    }


    private suspend fun safeDeleteReportCall(reportId: String) {
        deleteReport.postValue(Resource.Loading())
        try {
            val response = repository.deleteReport(reportId)
            if (response.isSuccessful) {
                response.body()?.let { profileResponse ->
                    deleteReport.postValue(Resource.Success(profileResponse))
                }
            } else {
                deleteReport.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> deleteReport.postValue(Resource.Error("Network Failure", null))
                else -> deleteReport.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }
}