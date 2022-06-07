package com.doctorsplaza.app.ui.patient.fragments.addAppointmentForm.model

import com.google.gson.annotations.SerializedName

data class PaymentDataModel(
     val success: Boolean,
     val message: String,
     val data: Payment
)

data class Payment(
     val _id: String
)