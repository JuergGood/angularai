package ch.goodone.angularai.android.data.repository

import ch.goodone.angularai.android.data.local.TaskDao
import ch.goodone.angularai.android.data.local.entity.TaskEntity
import ch.goodone.angularai.android.data.remote.TaskApi
import ch.goodone.angularai.android.data.remote.dto.TaskDTO
import ch.goodone.angularai.android.domain.model.Task
import ch.goodone.angularai.android.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskRepository @Inject constructor(
    private val api: TaskApi,
    private val dao: TaskDao
) {
    val tasks: Flow<List<Task>> = dao.getAllTasks().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun refreshTasks() {
        try {
            val remoteTasks = api.getTasks()
            dao.clearTasks()
            dao.insertTasks(remoteTasks.map { it.toEntity() })
        } catch (e: Exception) {
            // Handle error or use cache
        }
    }

    suspend fun createTask(task: Task) {
        val dto = task.toDto()
        val savedDto = api.createTask(dto)
        dao.insertTasks(listOf(savedDto.toEntity()))
    }

    suspend fun updateTask(task: Task) {
        val dto = task.toDto()
        val updatedDto = api.updateTask(task.id!!, dto)
        dao.insertTasks(listOf(updatedDto.toEntity()))
    }

    suspend fun deleteTask(id: Long) {
        api.deleteTask(id)
        // Ideally we should have a way to delete from local too, 
        // but for now refreshTasks will handle it or we can add a delete method to DAO.
        refreshTasks()
    }

    suspend fun reorderTasks(taskIds: List<Long>) {
        try {
            api.reorderTasks(taskIds)
            refreshTasks()
        } catch (e: Exception) {
            // Handle error
        }
    }

    private fun TaskEntity.toDomain() = Task(
        id, title, description, dueDate, priority,
        TaskStatus.valueOf(status), position
    )

    private fun TaskDTO.toEntity() = TaskEntity(
        id!!, title, description, dueDate, priority,
        status, position
    )

    private fun Task.toDto() = TaskDTO(
        id, title, description, dueDate, priority,
        status.name, position
    )
}
