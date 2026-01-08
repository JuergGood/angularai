package ch.goodone.angularai.android.data.remote

import ch.goodone.angularai.android.data.remote.dto.LogResponseDTO
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Query

interface LogApi {
    @GET("api/admin/logs")
    suspend fun getLogs(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("actionType") actionType: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("sort") sort: String = "timestamp,desc"
    ): LogResponseDTO

    @DELETE("api/admin/logs")
    suspend fun clearLogs(): Response<Unit>
}
