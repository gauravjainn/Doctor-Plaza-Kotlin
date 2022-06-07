package com.doctorsplaza.app.ui.patient.commonModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doctorsplaza.app.data.Repository
import com.doctorsplaza.app.ui.patient.fragments.slugs.model.SlugsModel
import com.doctorsplaza.app.utils.Resource
import com.doctorsplaza.app.utils.SessionManager
import com.google.gson.JsonObject
import com.gym.gymapp.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class CommonViewModel @Inject constructor(private val repository: Repository,private val session: SessionManager):ViewModel() {

    val slugs = SingleLiveEvent<Resource<SlugsModel>>()
    val deletePatientAccount = SingleLiveEvent<Resource<CommonModel>>()
    val contactUs = SingleLiveEvent<Resource<CommonModel>>()

        fun deletePatientAccount(jsonObject: JsonObject) = viewModelScope.launch {
        safeDeletePatientAccountCall(jsonObject)
    }


    private suspend fun safeDeletePatientAccountCall(jsonObject: JsonObject) {
        deletePatientAccount.postValue(Resource.Loading())
        try {
            val response = repository.deletePatientAccount(session.patientId,jsonObject)
            if (response.isSuccessful) {
                response.body()?.let { deletePatientAccountResponse ->
                    deletePatientAccount.postValue(Resource.Success(deletePatientAccountResponse))
                }
            } else {
                deletePatientAccount.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> deletePatientAccount.postValue(Resource.Error("Network Failure", null))
                else -> deletePatientAccount.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }


    fun getSlugs() = viewModelScope.launch {
        safeGetSlugsCall()
    }


    private suspend fun safeGetSlugsCall() {
        slugs.postValue(Resource.Loading())
        try {
            val response = repository.getSlugs()
            if (response.isSuccessful) {
                response.body()?.let { slugsResponse ->
                    slugs.postValue(Resource.Success(slugsResponse))
                }
            } else {
                slugs.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> slugs.postValue(Resource.Error("Network Failure", null))
                else -> slugs.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }


    fun contactUs(jsonObject: JsonObject) = viewModelScope.launch {
        safeContactUsCall(jsonObject)
    }


    private suspend fun safeContactUsCall(jsonObject: JsonObject) {
        contactUs.postValue(Resource.Loading())
        try {
            val response = repository.contactUs(jsonObject)
            if (response.isSuccessful) {
                response.body()?.let { slugsResponse ->
                    contactUs.postValue(Resource.Success(slugsResponse))
                }
            } else {
                contactUs.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> contactUs.postValue(Resource.Error("Network Failure", null))
                else -> contactUs.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }
}