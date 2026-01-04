package ch.goodone.angularai.android.data.remote

import ch.goodone.angularai.android.data.remote.dto.UserDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(
        @Header("Authorization") authHeader: String
    ): Response<UserDTO>

    @POST("api/auth/register")
    suspend fun register(
        @Body user: UserDTO
    ): Response<UserDTO>

    @POST("api/auth/logout")
    suspend fun logout(): Response<Unit>
}
