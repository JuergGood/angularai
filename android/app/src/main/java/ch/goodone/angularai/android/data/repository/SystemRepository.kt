package ch.goodone.angularai.android.data.repository

import ch.goodone.angularai.android.data.remote.SystemApi
import ch.goodone.angularai.android.data.remote.dto.SystemInfoDTO
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemRepository @Inject constructor(
    private val api: SystemApi
) {
    suspend fun getSystemInfo(): SystemInfoDTO {
        return api.getSystemInfo()
    }
}
