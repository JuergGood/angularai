package ch.goodone.angularai.testclient.command

import ch.goodone.angularai.testclient.client.ApiClient
import ch.goodone.angularai.testclient.model.ActionLogDTO
import ch.goodone.angularai.testclient.model.TaskDTO
import ch.goodone.angularai.testclient.model.UserDTO
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock

class LoadCommandTest {
    private lateinit var client: ApiClient
    private lateinit var loadCommand: LoadCommand

    @BeforeEach
    fun setUp() {
        client = mock()
        loadCommand = LoadCommand()
    }

    @Test
    fun `test load users`() {
        loadCommand.execute(client, listOf("users"))
        verify(client, atLeastOnce()).post(eq("/api/admin/users"), any(), eq(UserDTO::class.java))
    }

    @Test
    fun `test load tasks`() {
        loadCommand.execute(client, listOf("tasks"))
        verify(client, atLeastOnce()).post(eq("/api/tasks"), any(), eq(TaskDTO::class.java))
    }

    @Test
    fun `test load logs`() {
        loadCommand.execute(client, listOf("logs"))
        verify(client, atLeastOnce()).post(eq("/api/admin/logs"), any(), eq(ActionLogDTO::class.java))
    }

    @Test
    fun `test load all`() {
        loadCommand.execute(client, listOf("all"))
        verify(client, atLeastOnce()).post(eq("/api/admin/users"), any(), eq(UserDTO::class.java))
        verify(client, atLeastOnce()).post(eq("/api/tasks"), any(), eq(TaskDTO::class.java))
        verify(client, atLeastOnce()).post(eq("/api/admin/logs"), any(), eq(ActionLogDTO::class.java))
    }

    @Test
    fun `test load paging tasks`() {
        loadCommand.execute(client, listOf("paging", "tasks", "--count", "5"))
        verify(client, times(5)).post(eq("/api/tasks"), any(), eq(TaskDTO::class.java))
    }

    @Test
    fun `test load paging logs`() {
        loadCommand.execute(client, listOf("paging", "logs", "--count", "3"))
        verify(client, times(3)).post(eq("/api/admin/logs"), any(), eq(ActionLogDTO::class.java))
    }
}
