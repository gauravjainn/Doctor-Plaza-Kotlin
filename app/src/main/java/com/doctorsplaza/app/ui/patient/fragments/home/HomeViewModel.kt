package com.doctorsplaza.app.ui.patient.fragments.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doctorsplaza.app.data.Repository
import com.doctorsplaza.app.ui.patient.commonModel.AppointmentsModel
import com.doctorsplaza.app.ui.patient.fragments.home.model.OurDoctorsModel
import com.doctorsplaza.app.ui.patient.fragments.home.model.OurSpecialistsModel
import com.doctorsplaza.app.ui.patient.fragments.home.model.PatientBannerModel
import com.doctorsplaza.app.ui.patient.fragments.search.model.SearchModel
import com.doctorsplaza.app.utils.Resource
import com.google.gson.JsonObject
import com.doctorsplaza.app.utils.SessionManager
import com.gym.gymapp.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: Repository,private val session: SessionManager) : ViewModel() {

    val patientBanner = SingleLiveEvent<Resource<PatientBannerModel>>()
    val ourSpecialists = SingleLiveEvent<Resource<OurSpecialistsModel>>()
    val ourDoctors = SingleLiveEvent<Resource<OurDoctorsModel>>()
    val appointments = SingleLiveEvent<Resource<AppointmentsModel>>()
    val search = SingleLiveEvent<Resource<SearchModel>>()

    var patientBannerLoaded = false
    var ourSpecialistsLoaded = false
    var ourDoctorsLoaded = false
    val allDataLoaded = MutableLiveData<Boolean>()


    fun  getAllDataLoaded(){

        allDataLoaded.postValue(patientBannerLoaded&&ourDoctorsLoaded&&ourSpecialistsLoaded)
    }

    fun getPatientBanners() = viewModelScope.launch {
        val json = JsonObject()
        json.addProperty("cat", "Users on app")
        safeCallPatientBanner(json)
    }

    private suspend fun safeCallPatientBanner(json: JsonObject) {
        patientBanner.postValue(Resource.Loading())
        try {
            val response = repository.getPatientBanner(json)
            if (response.isSuccessful) {
                response.body()?.let { trendingResponse ->
                    patientBanner.postValue(Resource.Success(trendingResponse))
                }
            } else {
                patientBanner.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> patientBanner.postValue(Resource.Error("Network Failure", null))
                else -> patientBanner.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

    fun getOurSpecialists() = viewModelScope.launch {
        safeCallSpecialists()
    }

    private suspend fun safeCallSpecialists() {
        ourSpecialists.postValue(Resource.Loading())
        try {
            val response = repository.getOurSpecialists()
            if (response.isSuccessful) {
                response.body()?.let { trendingResponse ->
                    ourSpecialists.postValue(Resource.Success(trendingResponse))
                }
            } else {
                ourSpecialists.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> ourSpecialists.postValue(Resource.Error("Network Failure", null))
                else -> ourSpecialists.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

    fun getOurDoctors() = viewModelScope.launch {
        safeCallOurDoctors()
    }


    private suspend fun safeCallOurDoctors() {
        ourDoctors.postValue(Resource.Loading())
        try {
            val response = repository.getOurDoctors("1","10")
            if (response.isSuccessful) {
                response.body()?.let { trendingResponse ->
                    ourDoctors.postValue(Resource.Success(trendingResponse))
                }
            } else {
                ourDoctors.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> ourDoctors.postValue(Resource.Error("Network Failure", null))
                else -> ourDoctors.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

    fun getAppointments() = viewModelScope.launch {
        safeAppointmentsCall()
    }

    private suspend fun safeAppointmentsCall() {
        appointments.postValue(Resource.Loading())
        try {
            val response = repository.getAppointments(session.patientId,"1","10","new")
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

    fun search(searchKey:String) = viewModelScope.launch {
        safeSearchCall(searchKey)
    }

    private suspend fun safeSearchCall(searchKey: String) {
        search.postValue(Resource.Loading())
        try {
            val response = repository.search(searchKey)
            if (response.isSuccessful) {
                response.body()?.let { appointmentsResponse ->
                    search.postValue(Resource.Success(appointmentsResponse))
                }
            } else {
                search.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> search.postValue(Resource.Error("Network Failure", null))
                else -> search.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }
}