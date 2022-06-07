package com.doctorsplaza.app.ui.patient.fragments.home.model

data class PatientBannerModel(
    val count: Int,
    val data: List<BannerData>,
    val status: Int
)


data class BannerData(
    val _id: String,
    val banner_category: String,
    val banner_image: String,
    val banner_title: String,
    val createdAt: String,
    val is_active: Boolean,
    val updatedAt: String
)