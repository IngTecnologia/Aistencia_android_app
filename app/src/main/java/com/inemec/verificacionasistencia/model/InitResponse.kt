package com.inemec.verificacionasistencia.model

import com.google.gson.annotations.SerializedName

data class InitResponse(
    val valid: Boolean,
    val nombre: String?,
    val mensaje: String?,
    @SerializedName("session_token")
    val session_token: String?
)

data class VerifyResponse(
    val verified: Boolean,
    val distance: Double,
    val cedula: String,
    @SerializedName("tipo_registro")
    val tipo_registro: String,
    val timestamp: String,
    @SerializedName("record_id")
    val record_id: String
)