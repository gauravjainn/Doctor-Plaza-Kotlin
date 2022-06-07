package com.doctorsplaza.app.ui.patient.fragments.clinicDoctors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doctorsplaza.app.data.Repository
import com.doctorsplaza.app.ui.patient.fragments.clinicDoctors.model.ClinicDoctorsModel
import com.doctorsplaza.app.ui.patient.fragments.home.model.OurDoctorsModel
import com.doctorsplaza.app.utils.Resource
import com.google.gson.JsonObject
import com.gym.gymapp.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ClinicDoctorsViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    val doctors = SingleLiveEvent<Resource<ClinicDoctorsModel>>()
    val allDoctorsDoctors = SingleLiveEvent<Resource<OurDoctorsModel>>()

    fun getAllDoctors(pageNo: String) = viewModelScope.launch {
        safeGetAllDoctorsCall(pageNo)
    }

    private suspend fun safeGetAllDoctorsCall(pageNo: String) {
        allDoctorsDoctors.postValue(Resource.Loading())
        try {
            val response = repository.getOurDoctors(pageNo,"10")
            if (response.isSuccessful) {
                response.body()?.let { clinicsResponse ->
                    allDoctorsDoctors.postValue(Resource.Success(clinicsResponse))
                }
            } else {
                allDoctorsDoctors.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> allDoctorsDoctors.postValue(Resource.Error("Network Failure", null))
                else -> allDoctorsDoctors.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

    fun getDoctorsByClinic(clinicId:String) = viewModelScope.launch {
        val jsonObject = JsonObject()
        jsonObject.addProperty("clinicId",clinicId)
        safeGetDoctorsByClinicCall(jsonObject)
    }

    private suspend fun safeGetDoctorsByClinicCall(jsonObject: JsonObject) {
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
    }
}