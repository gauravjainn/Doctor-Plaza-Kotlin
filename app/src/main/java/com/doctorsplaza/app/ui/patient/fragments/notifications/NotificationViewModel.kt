package com.doctorsplaza.app.ui.patient.fragments.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doctorsplaza.app.data.Repository
import com.doctorsplaza.app.ui.patient.fragments.notifications.model.NotificationModel
import com.doctorsplaza.app.utils.Resource
import com.google.gson.JsonObject
import com.doctorsplaza.app.utils.SessionManager
import com.gym.gymapp.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(private val repository: Repository, private val session: SessionManager):ViewModel(){
    val notification = SingleLiveEvent<Resource<NotificationModel>>()



    fun getNotifications(jsonObject: JsonObject) = viewModelScope.launch {
        safeGetNotificationCall(jsonObject)
    }

    private suspend fun safeGetNotificationCall(jsonObject: JsonObject) {
        notification.postValue(Resource.Loading())
        try {
            val response = repository.getPatientNotification(jsonObject)
            if (response.isSuccessful) {
                response.body()?.let { doctorResponse ->
                    notification.postValue(Resource.Success(doctorResponse))
                }
            } else {
                notification.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> notification.postValue(Resource.Error("Network Failure", null))
                else -> notification.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

    fun getDoctorNotifications(jsonObject: JsonObject) = viewModelScope.launch {
        safeGetDoctorNotificationCall(jsonObject)
    }

    private suspend fun safeGetDoctorNotificationCall(jsonObject: JsonObject) {
        notification.postValue(Resource.Loading())
        try {
            val response = repository.getDoctorNotification(jsonObject)
            if (response.isSuccessful) {
                response.body()?.let { doctorResponse ->
                    notification.postValue(Resource.Success(doctorResponse))
                }
            } else {
                notification.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> notification.postValue(Resource.Error("Network Failure", null))
                else -> notification.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }
}