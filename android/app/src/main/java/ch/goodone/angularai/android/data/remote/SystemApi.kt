package ch.goodone.angularai.android.data.remote

import ch.goodone.angularai.android.data.remote.dto.SystemInfoDTO
import retrofit2.http.GET

interface SystemApi {
    @GET("api/system/info")
    suspend fun getSystemInfo(): SystemInfoDTO
}
