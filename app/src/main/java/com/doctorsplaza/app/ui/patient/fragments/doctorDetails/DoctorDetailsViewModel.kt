package com.doctorsplaza.app.ui.patient.fragments.doctorDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doctorsplaza.app.data.Repository
import com.doctorsplaza.app.ui.patient.fragments.addAppointmentForm.model.GetAppointmentsClinicsModel
import com.doctorsplaza.app.ui.patient.fragments.doctorDetails.model.DoctorDetailsModel
import com.doctorsplaza.app.utils.Resource
import com.gym.gymapp.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class DoctorDetailsViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    val doctor = SingleLiveEvent<Resource<DoctorDetailsModel>>()


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




}