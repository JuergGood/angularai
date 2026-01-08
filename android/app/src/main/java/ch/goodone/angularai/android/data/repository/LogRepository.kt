package ch.goodone.angularai.android.data.repository

import ch.goodone.angularai.android.data.remote.LogApi
import ch.goodone.angularai.android.data.remote.dto.LogResponseDTO
import javax.inject.Inject

class LogRepository @Inject constructor(
    private val api: LogApi
) {
    suspend fun getLogs(
        page: Int,
        size: Int,
        actionType: String?,
        startDate: String?,
        endDate: String?
    ): LogResponseDTO {
        val typeParam = if (actionType == "all") null else actionType
        return api.getLogs(
            page = page,
            size = size,
            actionType = typeParam,
            startDate = startDate,
            endDate = endDate
        )
    }

    suspend fun clearLogs() {
        api.clearLogs()
    }
}
