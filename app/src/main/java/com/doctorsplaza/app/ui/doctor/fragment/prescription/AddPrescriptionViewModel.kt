package com.doctorsplaza.app.ui.doctor.fragment.prescription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doctorsplaza.app.data.Repository
import com.doctorsplaza.app.ui.doctor.fragment.prescription.model.AddPrescriptionModel
import com.doctorsplaza.app.ui.doctor.fragment.prescription.model.MedicineModel
import com.doctorsplaza.app.utils.Resource
import com.doctorsplaza.app.utils.SessionManager
import com.doctorsplaza.app.utils.checkResponseBody
import com.doctorsplaza.app.utils.checkThrowable
import com.google.gson.JsonObject
import com.gym.gymapp.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class AddPrescriptionViewModel @Inject constructor(
    private val repository: Repository,
    val session: SessionManager
) : ViewModel() {

    val addMedicine = SingleLiveEvent<MedicineModel>()

    val addPrescription = SingleLiveEvent<Resource<AddPrescriptionModel>>()

    fun addPrescription(jsonObject: JsonObject) = viewModelScope.launch { safeAddPrescriptionCall(jsonObject) }

    private suspend fun safeAddPrescriptionCall(jsonObject: JsonObject) {
        addPrescription.postValue(Resource.Loading())
        try {
            val response = repository.addPrescription(jsonObject)
            if (response.isSuccessful)
                addPrescription.postValue(Resource.Success(checkResponseBody(response.body()) as AddPrescriptionModel))
            else
                addPrescription.postValue(Resource.Error(response.message(), null))
        } catch (t: Throwable) {
            addPrescription.postValue(Resource.Error(checkThrowable(t), null))
        }
    }
}