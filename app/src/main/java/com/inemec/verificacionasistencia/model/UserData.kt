package com.inemec.verificacionasistencia.model

object UserData {
    var cedula: String = ""
    var tipoRegistro: String = ""
    var sessionToken: String = ""
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var fueraUbicacion: Boolean = false
    var requiereComentario: Boolean = false
    var comentario: String? = null
    var ubicacionActual: String? = null
    var distancia: Int = 0
}