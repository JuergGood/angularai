package ch.goodone.angularai.android.data.remote

import ch.goodone.angularai.android.data.remote.dto.UserDTO
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
    @GET("api/system/info")
    suspend fun getSystemInfo(): ch.goodone.angularai.android.data.remote.dto.SystemInfoDTO

    @GET("api/users/me")
    suspend fun getCurrentUser(): UserDTO

    @PUT("api/users/me")
    suspend fun updateCurrentUser(@Body user: UserDTO): UserDTO

    @GET("api/admin/users")
    suspend fun getAllUsers(): List<UserDTO>

    @POST("api/admin/users")
    suspend fun createUser(@Body user: UserDTO): UserDTO

    @PUT("api/admin/users/{id}")
    suspend fun updateUser(@Path("id") id: Long, @Body user: UserDTO): UserDTO

    @DELETE("api/admin/users/{id}")
    suspend fun deleteUser(@Path("id") id: Long): Response<Unit>
}
