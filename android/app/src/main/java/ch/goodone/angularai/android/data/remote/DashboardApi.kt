package ch.goodone.angularai.android.data.remote

import ch.goodone.angularai.android.data.remote.dto.DashboardDTO
import retrofit2.http.GET

interface DashboardApi {
    @GET("api/dashboard")
    suspend fun getDashboard(): DashboardDTO
}
