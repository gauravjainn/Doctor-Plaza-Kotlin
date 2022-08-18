package com.doctorsplaza.app.ui.patient.fragments.home.model

import java.io.Serializable

data class OurSpecialistsModel(
    val count: Int,
    val data: List<SpecialistData>,
    val message: String,
    val status: Int
):Serializable

data class SpecialistData(
    val _id: String,
    val condition: String,
    val createdAt: String,
    val name: String,
    val image: String,
    val searchtype: String,
    val specialization: String,
    val status: Boolean,
    val updatedAt: String
):Serializable