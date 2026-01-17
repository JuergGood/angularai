package ch.goodone.angularai.android

import ch.goodone.angularai.android.data.local.TaskDao
import ch.goodone.angularai.android.data.remote.AuthApi
import ch.goodone.angularai.android.data.remote.TaskApi
import ch.goodone.angularai.android.data.remote.UserApi
import ch.goodone.angularai.android.data.remote.dto.TaskDTO
import ch.goodone.angularai.android.data.remote.dto.UserDTO
import ch.goodone.angularai.android.data.repository.AuthRepository
import ch.goodone.angularai.android.data.repository.TaskRepository
import ch.goodone.angularai.android.data.repository.UserRepository
import ch.goodone.angularai.android.domain.model.Task
import ch.goodone.angularai.android.domain.model.TaskStatus
import ch.goodone.angularai.android.domain.model.User
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import retrofit2.Response

class RepositoryTest {

    @Mock
    lateinit var taskApi: TaskApi

    @Mock
    lateinit var taskDao: TaskDao

    @Mock
    lateinit var authApi: AuthApi

    @Mock
    lateinit var userApi: UserApi

    @Mock
    lateinit var dataStore: androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>

    lateinit var taskRepository: TaskRepository
    lateinit var userRepository: UserRepository
    lateinit var authRepository: AuthRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        taskRepository = TaskRepository(taskApi, taskDao)
        userRepository = UserRepository(userApi)
        authRepository = AuthRepository(authApi, userRepository, dataStore)
    }

    @Test
    fun refreshTasks_shouldFetchFromApiAndSaveToDao() = runBlocking {
        val remoteTasks = listOf(TaskDTO(1L, "Title", "Desc", "2024-01-01", "HIGH", "OPEN", 0))
        `when`(taskApi.getTasks()).thenReturn(remoteTasks)

        taskRepository.refreshTasks()

        verify(taskApi).getTasks()
        verify(taskDao).clearTasks()
        verify(taskDao).insertTasks(anyList())
    }

    @Test
    fun createTask_shouldCallApiAndSaveToDao() = runBlocking {
        val task = Task(null, "New Task", "Desc", "2024-01-01", "MEDIUM")
        val savedDto = TaskDTO(1L, "New Task", "Desc", "2024-01-01", "MEDIUM", "OPEN", 0)
        `when`(taskApi.createTask(any())).thenReturn(savedDto)

        taskRepository.createTask(task)

        verify(taskApi).createTask(any())
        verify(taskDao).insertTasks(anyList())
    }

    @Test
    fun updateTask_shouldCallApiAndSaveToDao() = runBlocking {
        val task = Task(1L, "Updated Task", "Desc", "2024-01-01", "MEDIUM", TaskStatus.IN_PROGRESS, 1)
        val updatedDto = TaskDTO(1L, "Updated Task", "Desc", "2024-01-01", "MEDIUM", "IN_PROGRESS", 1)
        `when`(taskApi.updateTask(eq(1L), any())).thenReturn(updatedDto)

        taskRepository.updateTask(task)

        verify(taskApi).updateTask(eq(1L), any())
        verify(taskDao).insertTasks(anyList())
    }

    @Test
    fun deleteTask_shouldCallApiAndRefresh() = runBlocking {
        `when`(taskApi.getTasks()).thenReturn(emptyList())

        taskRepository.deleteTask(1L)

        verify(taskApi).deleteTask(1L)
        verify(taskApi).getTasks() // from refreshTasks
    }

    @Test
    fun register_shouldCallApiAndReturnResult() = runBlocking {
        val user = User(null, "New", "User", "newuser", "new@e.c")
        val responseDto = UserDTO(1L, "New", "User", "newuser", "new@e.c", null, null, "ROLE_USER")
        `when`(authApi.register(any())).thenReturn(Response.success(responseDto))

        val result = authRepository.register(user, "password")

        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull()?.id)
        verify(authApi).register(any())
    }

    @Test
    fun getCurrentUser_shouldCallApi() = runBlocking {
        val dto = UserDTO(1L, "First", "Last", "login", "email")
        `when`(userApi.getCurrentUser()).thenReturn(dto)

        val result = userRepository.getCurrentUser()

        assertEquals("First", result.firstName)
        verify(userApi).getCurrentUser()
    }

    @Test
    fun getAllUsers_shouldCallApi() = runBlocking {
        val dtos = listOf(UserDTO(1L, "First", "Last", "login", "email"))
        `when`(userApi.getAllUsers()).thenReturn(dtos)

        val result = userRepository.getAllUsers()

        assertEquals(1, result.size)
        verify(userApi).getAllUsers()
    }
}
