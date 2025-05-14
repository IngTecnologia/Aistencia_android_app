package com.inemec.verificacionasistencia.model

import com.google.gson.annotations.SerializedName

data class VersionResponse(
    @SerializedName("latest_version")
    val latestVersion: String,
    @SerializedName("minimum_version")
    val minimumVersion: String,
    @SerializedName("force_update")
    val forceUpdate: Boolean,
    @SerializedName("update_message")
    val updateMessage: String,
    @SerializedName("download_url")
    val downloadUrl: String?
)