package com.doctorsplaza.app.ui.doctor.fragment.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doctorsplaza.app.data.Repository
import com.doctorsplaza.app.ui.doctor.loginSignUp.model.DoctorDashBoard
import com.doctorsplaza.app.ui.doctor.fragment.home.model.DoctorUpcomingAppointmentModel
import com.doctorsplaza.app.ui.patient.fragments.home.model.OurDoctorsModel
import com.doctorsplaza.app.ui.patient.fragments.home.model.OurSpecialistsModel
import com.doctorsplaza.app.ui.patient.fragments.home.model.PatientBannerModel
import com.doctorsplaza.app.ui.patient.fragments.search.model.SearchModel
import com.doctorsplaza.app.ui.videoCall.model.VideoTokenModel
import com.doctorsplaza.app.utils.Resource
import com.doctorsplaza.app.utils.SessionManager
import com.google.gson.JsonObject
import com.gym.gymapp.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class DoctorHomeViewModel @Inject constructor(
    private val repository: Repository,
    private val session: SessionManager
) : ViewModel() {

    val doctorBanner = SingleLiveEvent<Resource<PatientBannerModel>>()
    val ourSpecialists = SingleLiveEvent<Resource<OurSpecialistsModel>>()
    val ourDoctors = SingleLiveEvent<Resource<OurDoctorsModel>>()
    val appointments = SingleLiveEvent<Resource<DoctorUpcomingAppointmentModel>>()
    val pastAppointments = SingleLiveEvent<Resource<DoctorUpcomingAppointmentModel>>()
    val dashBoard = SingleLiveEvent<Resource<DoctorDashBoard>>()
    val search = SingleLiveEvent<Resource<SearchModel>>()

    var doctorBannerLoaded = false
    var ourSpecialistsLoaded = false
    var ourDoctorsLoaded = false
    val allDataLoaded = MutableLiveData<Boolean>()
    val generateVideoToken = SingleLiveEvent<Resource<VideoTokenModel>>()


    fun getAllDataLoaded() {

        allDataLoaded.postValue(doctorBannerLoaded && ourDoctorsLoaded && ourSpecialistsLoaded)
    }

    fun getDoctorBanners() = viewModelScope.launch {
        val json = JsonObject()
        json.addProperty("cat", "Users on app")
        safeCallDoctorBanner(json)
    }

    private suspend fun safeCallDoctorBanner(json: JsonObject) {
        doctorBanner.postValue(Resource.Loading())
        try {
            val response = repository.getPatientBanner(json)
            if (response.isSuccessful) {
                response.body()?.let { trendingResponse ->
                    doctorBanner.postValue(Resource.Success(trendingResponse))
                }
            } else {
                doctorBanner.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> doctorBanner.postValue(Resource.Error("Network Failure", null))
                else -> doctorBanner.postValue(
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
            val response = repository.getOurDoctors("1", "10")
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

    fun getDoctorUpcomingAppointments(json: JsonObject) = viewModelScope.launch {
        safeDoctorUpcomingAppointmentsCall(json)
    }

    private suspend fun safeDoctorUpcomingAppointmentsCall(json: JsonObject) {
        appointments.postValue(Resource.Loading())
        try {
            val response = repository.getDoctorUpComingAppointments("new", json)
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

    fun getDoctorPastAppointments(json: JsonObject) = viewModelScope.launch {
        safeDoctorPastAppointmentsCall(json)
    }

    private suspend fun safeDoctorPastAppointmentsCall(json: JsonObject) {
        pastAppointments.postValue(Resource.Loading())
        try {
            val response = repository.getDoctorUpComingAppointments("old", json)
            if (response.isSuccessful) {
                response.body()?.let { appointmentsResponse ->
                    pastAppointments.postValue(Resource.Success(appointmentsResponse))
                }
            } else {
                pastAppointments.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> pastAppointments.postValue(Resource.Error("Network Failure", null))
                else -> pastAppointments.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

    fun getDoctorDashBoardData(json: JsonObject) = viewModelScope.launch {
        safeDoctorDashBoardDataCall(json)
    }

    private suspend fun safeDoctorDashBoardDataCall(json: JsonObject) {
        dashBoard.postValue(Resource.Loading())
        try {
            val response = repository.getDoctorDashBoardData(json)
            if (response.isSuccessful) {
                response.body()?.let { appointmentsResponse ->
                    dashBoard.postValue(Resource.Success(appointmentsResponse))
                }
            } else {
                dashBoard.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> dashBoard.postValue(Resource.Error("Network Failure", null))
                else -> dashBoard.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

    fun search(searchKey: String) = viewModelScope.launch {
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