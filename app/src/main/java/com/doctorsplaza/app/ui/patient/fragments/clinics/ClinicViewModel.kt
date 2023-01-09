package com.doctorsplaza.app.ui.patient.fragments.clinics

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doctorsplaza.app.data.Repository
import com.doctorsplaza.app.ui.patient.fragments.clinics.model.ClinicModel
import com.doctorsplaza.app.utils.Resource
import com.gym.gymapp.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ClinicViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    val clinics = SingleLiveEvent<Resource<ClinicModel>>()

    fun getClinics() = viewModelScope.launch {

        safeGetClinicsCall()
    }

    private suspend fun safeGetClinicsCall() {
        clinics.postValue(Resource.Loading())
        try {
            val response = repository.getClinics()
            if (response.isSuccessful) {
                response.body()?.let { clinicsResponse ->
                    clinics.postValue(Resource.Success(clinicsResponse))
                }
            } else {
                clinics.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            Log.e("TAG",""+t.message)
            when (t) {
                is IOException -> clinics.postValue(Resource.Error("Network Failure", null))
                else -> clinics.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }
}