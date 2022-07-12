package com.doctorsplaza.app.ui.videoCall.model

data class VideoTokenModel(
    val status: Int,
    val code: Int,
    val data: VideoTokenData,
    val message: String,
    val result: Result
)

data class VideoTokenData(
    val patients_token:String,
    val doctor_token:String
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