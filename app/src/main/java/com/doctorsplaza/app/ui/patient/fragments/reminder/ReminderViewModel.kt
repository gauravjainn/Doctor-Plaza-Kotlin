package com.doctorsplaza.app.ui.patient.fragments.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doctorsplaza.app.data.Repository
import com.doctorsplaza.app.ui.patient.fragments.reminder.model.AddReminderModel
import com.doctorsplaza.app.ui.patient.fragments.reminder.model.GetReminderModel
import com.doctorsplaza.app.ui.patient.fragments.reminder.model.UpdateReminderModel
import com.doctorsplaza.app.utils.Resource
import com.google.gson.JsonObject
import com.doctorsplaza.app.utils.SessionManager
import com.gym.gymapp.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(private val repository: Repository,private val session: SessionManager):ViewModel(){
    val addReminder = SingleLiveEvent<Resource<AddReminderModel>>()
    val getReminders = SingleLiveEvent<Resource<GetReminderModel>>()
    val updateReminder = SingleLiveEvent<Resource<UpdateReminderModel>>()


    fun addReminder(jsonObject: JsonObject) = viewModelScope.launch {
        safeAddReminderCall(jsonObject)
    }


    private suspend fun safeAddReminderCall(jsonObject: JsonObject) {
        addReminder.postValue(Resource.Loading())
        try {
            val response = repository.addReminder(jsonObject)
            if (response.isSuccessful) {
                response.body()?.let { doctorResponse ->
                    addReminder.postValue(Resource.Success(doctorResponse))
                }
            } else {
                addReminder.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> addReminder.postValue(Resource.Error("Network Failure", null))
                else -> addReminder.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

 fun updateReminder(reminderId: String, jsonObject: JsonObject) = viewModelScope.launch {
        safeUpdateReminderCall(reminderId,jsonObject)
    }


    private suspend fun safeUpdateReminderCall(reminderId: String,jsonObject: JsonObject) {
        updateReminder.postValue(Resource.Loading())
        try {
            val response = repository.updateReminder(reminderId,jsonObject)
            if (response.isSuccessful) {
                response.body()?.let { doctorResponse ->
                    updateReminder.postValue(Resource.Success(doctorResponse))
                }
            } else {
                updateReminder.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> updateReminder.postValue(Resource.Error("Network Failure", null))
                else -> updateReminder.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

    fun getReminders(date: String) = viewModelScope.launch {
        safeGetRemindersCall(date)
    }


    private suspend fun safeGetRemindersCall(date: String) {
        getReminders.postValue(Resource.Loading())
        try {
            val response = repository.getReminder(session.patientId,date)
            if (response.isSuccessful) {
                response.body()?.let { doctorResponse ->
                    getReminders.postValue(Resource.Success(doctorResponse))
                }
            } else {
                getReminders.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> getReminders.postValue(Resource.Error("Network Failure", null))
                else -> getReminders.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }
}