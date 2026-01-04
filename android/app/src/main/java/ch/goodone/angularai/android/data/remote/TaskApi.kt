package ch.goodone.angularai.android.data.remote

import ch.goodone.angularai.android.data.remote.dto.TaskDTO
import retrofit2.Response
import retrofit2.http.*

interface TaskApi {
    @GET("api/tasks")
    suspend fun getTasks(): List<TaskDTO>

    @POST("api/tasks")
    suspend fun createTask(@Body task: TaskDTO): TaskDTO

    @PUT("api/tasks/{id}")
    suspend fun updateTask(@Path("id") id: Long, @Body task: TaskDTO): TaskDTO

    @DELETE("api/tasks/{id}")
    suspend fun deleteTask(@Path("id") id: Long): Response<Unit>
}
