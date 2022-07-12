package com.doctorsplaza.app.ui.patient.fragments.profile.model

data class ProfileImageUploadModel(
    val code: Int,
    val status: Int,
    val message: String,
    val profile_picture: String
)