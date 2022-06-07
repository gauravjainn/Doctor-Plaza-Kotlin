package com.doctorsplaza.app.ui.patient.fragments.profile.model

data class UpdateProfileModel(
    val code: Int,
    val `data`: UpdatedProfileData,
    val message: String,
    val result: Result
)
data class UpdatedProfileData(
    val address: String,
    val blood_group: String,
    val gender: String,
    val patient_name: String,
    val dob: String
)

data class Result(
    val `$clusterTime`: ClusterTime,
    val electionId: String,
    val n: Int,
    val nModified: Int,
    val ok: Int,
    val opTime: OpTime,
    val operationTime: String
)

data class ClusterTime(
    val clusterTime: String,
    val signature: Signature
)

data class OpTime(
    val t: Int,
    val ts: String
)
data class Signature(
    val hash: String,
    val keyId: String
)