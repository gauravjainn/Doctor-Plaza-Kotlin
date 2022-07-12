package com.doctorsplaza.app.ui.doctor.loginSignUp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doctorsplaza.app.data.Repository
import com.doctorsplaza.app.ui.doctor.loginSignUp.model.*
import com.doctorsplaza.app.data.commonModel.CommonModel
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
class DoctorLoginSignUpViewModel @Inject constructor(private val repository: Repository) :
    ViewModel() {

    val checkEmail = SingleLiveEvent<Resource<CommonModel>>()
    val doctorReg = SingleLiveEvent<Resource<DoctorSignupModel>>()
    val verifyOTP = SingleLiveEvent<Resource<OTPModel>>()
    val login = SingleLiveEvent<Resource<DoctorLoginModel>>()


    val specializations = SingleLiveEvent<Resource<SpecialistsModel>>()
    val city = SingleLiveEvent<Resource<CityModel>>()
    val state = SingleLiveEvent<Resource<StateModel>>()

    val resendLoginOtp = SingleLiveEvent<Resource<CommonModel>>()
    val resendSignUpOtp = SingleLiveEvent<Resource<CommonModel>>()

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

    fun doctorRegister(jsonObject: JsonObject) = viewModelScope.launch {
        safeDoctorRegisterCall(jsonObject)
    }

    private suspend fun safeDoctorRegisterCall(jsonObject: JsonObject) {
        doctorReg.postValue(Resource.Loading())
        try {
            val response = repository.doctorRegister(jsonObject)
            if (response.isSuccessful) {
                response.body()?.let { stateResponse ->
                    doctorReg.postValue(Resource.Success(stateResponse))
                }
            }else{

                doctorReg.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> doctorReg.postValue(Resource.Error("Network Failure", null))
                else -> doctorReg.postValue(Resource.Error("Conversion Error ${t.message}", null))
            }
        }
    }

    fun setLogin(jsonObject: JsonObject) = viewModelScope.launch {
        safeLoginCall(jsonObject)
    }

    private suspend fun safeLoginCall(jsonObject: JsonObject) {
        login.postValue(Resource.Loading())
        try {
            val response = repository.doctorLogin(jsonObject)
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
            val response = repository.doctorVerifyOTP(jsonObject)
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
            val response = repository.doctorVerifyOtpLogin(jsonObject)
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



    fun getSpecialization() = viewModelScope.launch {
        safeSpecializationCall()
    }

    private suspend fun safeSpecializationCall() {
        specializations.postValue(Resource.Loading())
        try {
            val response = repository.getSpecialization()
            if (response.isSuccessful) {
                response.body()?.let { stateResponse ->
                    specializations.postValue(Resource.Success(stateResponse))
                }
            }else{
                specializations.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> specializations.postValue(Resource.Error("Network Failure", null))
                else -> specializations.postValue(Resource.Error("Conversion Error ${t.message}", null))
            }
        }
    }

    fun getState() = viewModelScope.launch {
        safeGetStateCall()
    }

    private suspend fun safeGetStateCall() {
        state.postValue(Resource.Loading())
        try {
            val response = repository.getState()
            if (response.isSuccessful) {
                response.body()?.let { stateResponse ->
                    state.postValue(Resource.Success(stateResponse))
                }
            }else{
                state.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> state.postValue(Resource.Error("Network Failure", null))
                else -> state.postValue(Resource.Error("Conversion Error ${t.message}", null))
            }
        }
    }

    fun getCities(jsonObject: JsonObject) = viewModelScope.launch {
        safeGetCitiesCall(jsonObject)
    }

    private suspend fun safeGetCitiesCall(jsonObject: JsonObject) {
        city.postValue(Resource.Loading())
        try {
            val response = repository.getCity(jsonObject)
            if (response.isSuccessful) {
                response.body()?.let { stateResponse ->
                    city.postValue(Resource.Success(stateResponse))
                }
            }else{
                city.postValue(Resource.Error(response.message(), null))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> city.postValue(Resource.Error("Network Failure", null))
                else -> city.postValue(Resource.Error("Conversion Error ${t.message}", null))
            }
        }
    }




    fun resendLoginOtp(jsonObject: JsonObject) = viewModelScope.launch {
        safeResendLoginOtpCall(jsonObject)
    }

    private suspend fun safeResendLoginOtpCall(jsonObject: JsonObject) {
        resendLoginOtp.postValue(Resource.Loading())
        try {
            val response = repository.resendDoctorLoginOtp(jsonObject)
            if (response.isSuccessful)
                resendLoginOtp.postValue(Resource.Success(checkResponseBody(response.body()) as CommonModel))
            else
                resendLoginOtp.postValue(Resource.Error(response.message(), null))

        } catch (t: Throwable) {
            resendLoginOtp.postValue(Resource.Error(checkThrowable(t), null))
        }
    }

    fun resendSignUpOtp(jsonObject: JsonObject) = viewModelScope.launch {
        safeResendSignUpOtpCall(jsonObject)
    }

    private suspend fun safeResendSignUpOtpCall(jsonObject: JsonObject) {
        resendSignUpOtp.postValue(Resource.Loading())
        try {
            val response = repository.resendDoctorSignUpOtp(jsonObject)
            if (response.isSuccessful)
                resendSignUpOtp.postValue(Resource.Success(checkResponseBody(response.body()) as CommonModel))
            else
                resendSignUpOtp.postValue(Resource.Error(response.message(), null))

        } catch (t: Throwable) {
            resendSignUpOtp.postValue(Resource.Error(checkThrowable(t), null))
        }
    }
}