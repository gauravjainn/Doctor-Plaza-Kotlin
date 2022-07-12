package com.doctorsplaza.app.ui.patient.fragments.home.model

data class VideosModel(
    val data: List<VideoData>,
    val message: String,
    val status: Int,
    val total: Int
)

data class VideoData(
    val _id: String,
    val createdAt: String,
    val doctorData: String,
    val slug: String,
    val thumbnail: String,
    val title: String,
    val updatedAt: String,
    val url: String,
    val video_id: String
)