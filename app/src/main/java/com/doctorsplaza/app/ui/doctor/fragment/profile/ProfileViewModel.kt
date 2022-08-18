package com.doctorsplaza.app.ui.doctor.fragment.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doctorsplaza.app.data.Repository
import com.doctorsplaza.app.ui.doctor.fragment.profile.model.DoctorProfileModel
import com.doctorsplaza.app.ui.doctor.loginSignUp.model.SpecialistsModel
import com.doctorsplaza.app.data.commonModel.CommonModel
import com.doctorsplaza.app.ui.patient.fragments.profile.model.ProfileImageUploadModel
import com.doctorsplaza.app.utils.Resource
import com.doctorsplaza.app.utils.SessionManager
import com.doctorsplaza.app.utils.*
import com.google.gson.JsonObject
import com.gym.gymapp.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.io.File
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class DoctorProfileViewModel @Inject constructor(
    private val repository: Repository,
    private val session: SessionManager
) : ViewModel() {

    val doctorProfile = SingleLiveEvent<Resource<DoctorProfileModel>>()
    val editDoctorProfile = SingleLiveEvent<Resource<CommonModel>>()
    val specializations = SingleLiveEvent<Resource<SpecialistsModel>>()
    val uploadDoctorImage = SingleLiveEvent<Resource<ProfileImageUploadModel>>()
    val doctorTurnDayOn = SingleLiveEvent<Resource<CommonModel>>()
    val doctorTurnDayOff = SingleLiveEvent<Resource<CommonModel>>()
    val refreshToken = SingleLiveEvent<Resource<CommonModel>>()

    val logout = SingleLiveEvent<Resource<CommonModel>>()

    fun getDoctorProfile() = viewModelScope.launch { safeDoctorProfileCall() }

    private suspend fun safeDoctorProfileCall() {
        doctorProfile.postValue(Resource.Loading())
        try {
            val response = repository.getDoctorProfile(session.loginId.toString())
            if (response.isSuccessful)
                doctorProfile.postValue(Resource.Success(checkResponseBody(response.body()) as DoctorProfileModel))
            else
                doctorProfile.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            doctorProfile.postValue(Resource.Error(checkThrowable(t), null))
        }
    }



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



    fun editDoctorProfile(jsonObject: JsonObject) =
        viewModelScope.launch { safeDoctorEditProfileCall(jsonObject) }

    private suspend fun safeDoctorEditProfileCall(jsonObject: JsonObject) {
        editDoctorProfile.postValue(Resource.Loading())
        try {
            val response = repository.editDoctorProfile(jsonObject)
            if (response.isSuccessful)
                editDoctorProfile.postValue(Resource.Success(checkResponseBody(response.body()) as CommonModel))
            else
                editDoctorProfile.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            editDoctorProfile.postValue(Resource.Error(checkThrowable(t), null))
        }
    }


    fun getSpecialization() = viewModelScope.launch {
        safeSpecializationCall()
    }

    private suspend fun safeSpecializationCall() {
        specializations.postValue(Resource.Loading())
        try {
            val response = repository.getSpecialization()
            if (response.isSuccessful)
                specializations.postValue(Resource.Success(checkResponseBody(response.body()) as SpecialistsModel))
            else
                specializations.postValue(Resource.Error(response.message(), null))

        } catch (t: Throwable) {
            specializations.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun uploadDoctorImage(file: File) = viewModelScope.launch {
        val profileImage = if (file.path.isEmpty()) {
            getRequestBodyFromFile("file", null)
        } else {
            getRequestBodyFromFile("file", file)
        }
        safeUploadDoctorImageCall(profileImage)
    }

    private suspend fun safeUploadDoctorImageCall(profileImage: MultipartBody.Part?) {
        uploadDoctorImage.postValue(Resource.Loading())
        try {
            val response = repository.uploadDoctorProfile(session.loginId.toString(), profileImage)
            if (response.isSuccessful)
                uploadDoctorImage.postValue(Resource.Success(checkResponseBody(response.body()) as ProfileImageUploadModel))
            else
                uploadDoctorImage.postValue(Resource.Error(response.message(), null))

        } catch (t: Throwable) {
            uploadDoctorImage.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun doctorTurnDayOn(jsonObject: JsonObject) = viewModelScope.launch {
        safeDoctorTurnDayOnCall(jsonObject)
    }

    private suspend fun safeDoctorTurnDayOnCall(jsonObject: JsonObject) {
        doctorTurnDayOn.postValue(Resource.Loading())
        try {
            val response = repository.doctorTurnDayOn(jsonObject)
            if (response.isSuccessful)
                doctorTurnDayOn.postValue(Resource.Success(checkResponseBody(response.body()) as CommonModel))
            else
                doctorTurnDayOn.postValue(Resource.Error(response.message(), null))

        } catch (t: Throwable) {
            doctorTurnDayOn.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun doctorTurnDayOff(jsonObject: JsonObject) = viewModelScope.launch {
        safeDoctorTurnDayOffCall(jsonObject)
    }

    private suspend fun safeDoctorTurnDayOffCall(jsonObject: JsonObject) {
        doctorTurnDayOn.postValue(Resource.Loading())
        try {
            val response = repository.doctorTurnDayOff(jsonObject)
            if (response.isSuccessful)
                doctorTurnDayOn.postValue(Resource.Success(checkResponseBody(response.body()) as CommonModel))
            else
                doctorTurnDayOn.postValue(Resource.Error(response.message(), null))

        } catch (t: Throwable) {
            doctorTurnDayOn.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun logout(jsonObject: JsonObject) = viewModelScope.launch {
        safeLogOutCall(jsonObject)
    }

    private suspend fun safeLogOutCall(jsonObject: JsonObject) {
        logout.postValue(Resource.Loading())
        try {
            val response = repository.logout(jsonObject)
            if (response.isSuccessful)
                logout.postValue(Resource.Success(checkResponseBody(response.body()) as CommonModel))
            else
                logout.postValue(Resource.Error(response.message(), null))

        } catch (t: Throwable) {
            logout.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

}