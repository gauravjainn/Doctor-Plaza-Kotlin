package com.doctorsplaza.app.ui.patient.loginSignUp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doctorsplaza.app.data.Repository
import com.doctorsplaza.app.data.commonModel.CommonModel
import com.doctorsplaza.app.ui.patient.loginSignUp.model.LoginModel
import com.doctorsplaza.app.ui.patient.loginSignUp.model.PatientRegisterModel
import com.doctorsplaza.app.ui.patient.loginSignUp.model.VerificationModel
import com.doctorsplaza.app.utils.Resource
import com.doctorsplaza.app.utils.checkResponseBody
import com.doctorsplaza.app.utils.checkThrowable
import com.google.gson.JsonObject
import com.gym.gymapp.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class PatientLoginSignUpViewModel @Inject constructor(private val repository: Repository) :
    ViewModel() {

    val checkEmail = SingleLiveEvent<Resource<CommonModel>>()
    val patientReg = SingleLiveEvent<Resource<PatientRegisterModel>>()
    val verifyOTP = SingleLiveEvent<Resource<VerificationModel>>()
    val login = SingleLiveEvent<Resource<LoginModel>>()

    val resendLoginSignUpOtp = SingleLiveEvent<Resource<CommonModel>>()

     fun checkEmail(jsonObject: JsonObject) = viewModelScope.launch {
        safeCheckEmailCall(jsonObject)
    }

    private suspend fun safeCheckEmailCall(jsonObject: JsonObject) {
        checkEmail.postValue(Resource.Loading())
        try {
            val response = repository.checkEmailValidation(jsonObject)
            if (response.isSuccessful) {
                response.body()?.let { stateResponse ->
                    checkEmail.postValue(Resource.Success(stateResponse))
                }
            }else{

             checkEmail.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> checkEmail.postValue(Resource.Error("Network Failure", null))
                else -> checkEmail.postValue(Resource.Error("Conversion Error ${t.message}", null))
            }
        }
    }

    fun patientRegister(jsonObject: JsonObject) = viewModelScope.launch {
        safePatientRegisterCall(jsonObject)
    }

    private suspend fun safePatientRegisterCall(jsonObject: JsonObject) {
        patientReg.postValue(Resource.Loading())
        try {
            val response = repository.patientRegistration(jsonObject)
            if (response.isSuccessful) {
                response.body()?.let { stateResponse ->
                    patientReg.postValue(Resource.Success(stateResponse))
                }
            }else{

                patientReg.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> patientReg.postValue(Resource.Error("Network Failure", null))
                else -> patientReg.postValue(Resource.Error("Conversion Error ${t.message}", null))
            }
        }
    }

    fun setLogin(jsonObject: JsonObject) = viewModelScope.launch {
        safeLoginCall(jsonObject)
    }

    private suspend fun safeLoginCall(jsonObject: JsonObject) {
        login.postValue(Resource.Loading())
        try {
            val response = repository.login(jsonObject)
            if (response.isSuccessful) {
                response.body()?.let { stateResponse ->
                    login.postValue(Resource.Success(stateResponse))
                }
            }else{
                login.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> login.postValue(Resource.Error("Network Failure", null))
                else -> login.postValue(Resource.Error("Conversion Error ${t.message}", null))
            }
        }
    }

    fun verifyOTP(jsonObject: JsonObject) = viewModelScope.launch {
        safeVerifyOTPCall(jsonObject)
    }

    private suspend fun safeVerifyOTPCall(jsonObject: JsonObject) {
        verifyOTP.postValue(Resource.Loading())
        try {
            val response = repository.verifyOTP(jsonObject)
            if (response.isSuccessful) {
                response.body()?.let { stateResponse ->
                    verifyOTP.postValue(Resource.Success(stateResponse))
                }
            }else{
                verifyOTP.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> verifyOTP.postValue(Resource.Error("Network Failure", null))
                else -> verifyOTP.postValue(Resource.Error("Conversion Error ${t.message}", null))
            }
        }
    }

    fun verifyLoginOTP(jsonObject: JsonObject) = viewModelScope.launch {
        safeVerifyLoginOTPCall(jsonObject)
    }

    private suspend fun safeVerifyLoginOTPCall(jsonObject: JsonObject) {
        verifyOTP.postValue(Resource.Loading())
        try {
            val response = repository.verifyLoginOTP(jsonObject)
            if (response.isSuccessful) {
                response.body()?.let { stateResponse ->
                    verifyOTP.postValue(Resource.Success(stateResponse))
                }
            }else{
                verifyOTP.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> verifyOTP.postValue(Resource.Error("Network Failure", null))
                else -> verifyOTP.postValue(Resource.Error("Conversion Error ${t.message}", null))
            }
        }
    }

    fun resendLoginSignUpOtp(jsonObject: JsonObject) = viewModelScope.launch {
        safeDoctorTurnDayOffCall(jsonObject)
    }

    private suspend fun safeDoctorTurnDayOffCall(jsonObject: JsonObject) {
        resendLoginSignUpOtp.postValue(Resource.Loading())
        try {
            val response = repository.resendLoginOtp(jsonObject)
            if (response.isSuccessful)
                resendLoginSignUpOtp.postValue(Resource.Success(checkResponseBody(response.body()) as CommonModel))
            else
                resendLoginSignUpOtp.postValue(Resource.Error(response.message(), null))

        } catch (t: Throwable) {
            resendLoginSignUpOtp.postValue(Resource.Error(checkThrowable(t), null))
        }
    }
}