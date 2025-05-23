package com.inemec.verificacionasistencia.api

import com.inemec.verificacionasistencia.model.InitResponse
import com.inemec.verificacionasistencia.model.VerifyResponse
import com.inemec.verificacionasistencia.model.VersionResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap

interface ApiService {
    @FormUrlEncoded
    @POST("verify-web/init")
    fun verifyInit(
        @Field("cedula") cedula: String,
        @Field("lat") latitude: Double,
        @Field("lng") longitude: Double,
        @Field("tipo_registro") tipoRegistro: String
    ): Call<InitResponse>

    @Multipart
    @POST("verify-web/face")
    fun verifyFace(
        @Part("cedula") cedula: String,
        @Part("session_token") sessionToken: String,
        @Part image: MultipartBody.Part
    ): Call<VerifyResponse>

    // Nueva función que acepta parámetros adicionales incluyendo comentario
    @Multipart
    @POST("verify-web/face")
    fun verifyFaceWithComment(
        @PartMap params: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part
    ): Call<VerifyResponse>

    @GET("version")
    fun getVersion(): Call<VersionResponse>
}