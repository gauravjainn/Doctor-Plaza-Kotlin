package com.doctorsplaza.app.ui.patient.commonModel

data class CommonModel(
    val code: String,
    val success: Boolean,
    val status: String,
    val message: String,
    val order_id: String,
    val count: String
)
