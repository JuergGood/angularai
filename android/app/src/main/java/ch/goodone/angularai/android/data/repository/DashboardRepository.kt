package ch.goodone.angularai.android.data.repository

import ch.goodone.angularai.android.data.remote.DashboardApi
import ch.goodone.angularai.android.data.remote.dto.DashboardDTO
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepository @Inject constructor(
    private val dashboardApi: DashboardApi
) {
    suspend fun getDashboard(): DashboardDTO {
        return dashboardApi.getDashboard()
    }
}
