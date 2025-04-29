package com.inemec.verificacionasistencia.model

import com.google.gson.annotations.SerializedName

data class InitResponse(
    val valid: Boolean,
    val nombre: String?,
    val mensaje: String?,
    @SerializedName("session_token")
    val session_token: String?,
    @SerializedName("fuera_de_ubicacion")
    val fuera_de_ubicacion: Boolean = false,
    @SerializedName("requiere_comentario")
    val requiere_comentario: Boolean = false,
    @SerializedName("ubicacion_actual")
    val ubicacion_actual: String? = null,
    val distancia: Int = 0
)

data class VerifyResponse(
    val verified: Boolean,
    val distance: Double,
    val cedula: String,
    @SerializedName("tipo_registro")
    val tipo_registro: String,
    val timestamp: String,
    @SerializedName("record_id")
    val record_id: String,
    @SerializedName("fuera_de_ubicacion")
    val fuera_de_ubicacion: Boolean = false,
    val comentario: String? = null,
    @SerializedName("ubicacion_nombre")
    val ubicacion_nombre: String? = null
)