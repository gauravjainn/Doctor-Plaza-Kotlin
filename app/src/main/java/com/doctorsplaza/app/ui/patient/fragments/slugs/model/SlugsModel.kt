package com.doctorsplaza.app.ui.patient.fragments.slugs.model

data class SlugsModel(
    val `data`: List<SlugsData>,
    val message: String,
    val status: Int,
    val success: Boolean
)

data class SlugsData(
    val _id: String,
    val cms_id: String,
    val cms_type: String,
    val content: String,
    val createdAt: String,
    val description: String,
    val is_active: Boolean,
    val meta_data: String,
    val short_description: String,
    val title: String,
    val updatedAt: String
)