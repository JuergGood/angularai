package ch.goodone.angularai.android.data.remote.dto

data class SystemInfoDTO(
    val backendVersion: String,
    val frontendVersion: String,
    val mode: String,
    val landingMessage: String? = null
)
