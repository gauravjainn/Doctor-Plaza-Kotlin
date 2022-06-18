package com.doctorsplaza.app.ui.doctor.fragment.clinics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doctorsplaza.app.data.Repository
import com.doctorsplaza.app.ui.doctor.fragment.clinics.model.ClinicDetailsModel
import com.doctorsplaza.app.ui.doctor.fragment.clinics.model.ClinicsListModel
import com.doctorsplaza.app.utils.Resource
import com.doctorsplaza.app.utils.SessionManager
import com.google.gson.JsonObject
import com.gym.gymapp.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ClinicsViewModel @Inject constructor(
    private val repository: Repository,
    private val session: SessionManager
) : ViewModel() {

    val clinicsList = SingleLiveEvent<Resource<ClinicsListModel>>()
    val clinicsDetails = SingleLiveEvent<Resource<ClinicDetailsModel>>()


    fun getClinicsList(jsonObject: JsonObject) = viewModelScope.launch {
        safeGetClinicsListCall(jsonObject)
    }

    private suspend fun safeGetClinicsListCall(jsonObject: JsonObject) {
        clinicsList.postValue(Resource.Loading())
        try {
            val response = repository.getDoctorClinicList(jsonObject)
            if (response.isSuccessful) {
                response.body()?.let { stateResponse ->
                    clinicsList.postValue(Resource.Success(stateResponse))
                }
            }else{

                clinicsList.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> clinicsList.postValue(Resource.Error("Network Failure", null))
                else -> clinicsList.postValue(Resource.Error("Conversion Error ${t.message}", null))
            }
        }
    }


    fun getClinicsDetails(clinicId:String) = viewModelScope.launch {
        safeClinicsDetailsCall(clinicId)
    }

    private suspend fun safeClinicsDetailsCall(clinicId: String) {
        clinicsDetails.postValue(Resource.Loading())
        try {
            val response = repository.getClinicDetails(clinicId)
            if (response.isSuccessful) {
                response.body()?.let { stateResponse ->
                    clinicsDetails.postValue(Resource.Success(stateResponse))
                }
            }else{

                clinicsDetails.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> clinicsDetails.postValue(Resource.Error("Network Failure", null))
                else -> clinicsDetails.postValue(Resource.Error("Conversion Error ${t.message}", null))
            }
        }
    }

}