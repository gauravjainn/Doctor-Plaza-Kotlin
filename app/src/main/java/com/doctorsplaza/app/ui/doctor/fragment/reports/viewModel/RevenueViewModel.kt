package com.doctorsplaza.app.ui.doctor.fragment.reports.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doctorsplaza.app.data.Repository
import com.doctorsplaza.app.ui.doctor.fragment.reports.model.AppointmentsCountModel
import com.doctorsplaza.app.ui.doctor.fragment.reports.model.FinancialModel
import com.doctorsplaza.app.ui.doctor.fragment.reports.model.RentStatusModel
import com.doctorsplaza.app.utils.Resource
import com.doctorsplaza.app.utils.SessionManager
import com.google.gson.JsonObject
import com.gym.gymapp.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class RevenueViewModel @Inject constructor(
    private val repository: Repository,
    private val session: SessionManager
) : ViewModel() {

    val appointmentReports = SingleLiveEvent<Resource<AppointmentsCountModel>>()
    val financialReports = SingleLiveEvent<Resource<FinancialModel>>()
    val rentStatus = SingleLiveEvent<Resource<RentStatusModel>>()

     fun getAppointmentsReports(jsonObject: JsonObject) = viewModelScope.launch {
        safeAppointmentsReports(jsonObject)
    }

    private suspend fun safeAppointmentsReports(jsonObject: JsonObject) {
        appointmentReports.postValue(Resource.Loading())
        try {
            val response = repository.getAppointmentsReports(jsonObject)
            if (response.isSuccessful) {
                response.body()?.let { appointmentsResponse ->
                    appointmentReports.postValue(Resource.Success(appointmentsResponse))
                }
            } else {
                appointmentReports.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> appointmentReports.postValue(
                    Resource.Error(
                        "Network Failure",
                        null
                    )
                )
                else -> appointmentReports.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }

    fun getFinancialReports(jsonObject: JsonObject) = viewModelScope.launch {
        safeFinancialReports(jsonObject)
    }


    private suspend fun safeFinancialReports(jsonObject: JsonObject) {
        financialReports.postValue(Resource.Loading())
        try {
            val response = repository.getFinancialReports(jsonObject)
            if (response.isSuccessful) {
                response.body()?.let { financialResponse ->
                    financialReports.postValue(Resource.Success(financialResponse))
                }
            } else {
                financialReports.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> financialReports.postValue(
                    Resource.Error(
                        "Network Failure",
                        null
                    )
                )
                else -> financialReports.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }


    fun getRentStatus(jsonObject: JsonObject) = viewModelScope.launch {
        safeRentStatusCall(jsonObject)
    }


    private suspend fun safeRentStatusCall(jsonObject: JsonObject) {
        rentStatus.postValue(Resource.Loading())
        try {
            val response = repository.getRentStatus(jsonObject)
            if (response.isSuccessful) {
                response.body()?.let { financialResponse ->
                    rentStatus.postValue(Resource.Success(financialResponse))
                }
            } else {
                rentStatus.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> rentStatus.postValue(
                    Resource.Error(
                        "Network Failure",
                        null
                    )
                )
                else -> rentStatus.postValue(
                    Resource.Error(
                        "Conversion Error ${t.message}",
                        null
                    )
                )
            }
        }
    }
}