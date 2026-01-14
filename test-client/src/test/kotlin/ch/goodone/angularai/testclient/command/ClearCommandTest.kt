package ch.goodone.angularai.testclient.command

import ch.goodone.angularai.testclient.client.ApiClient
import ch.goodone.angularai.testclient.model.TaskDTO
import ch.goodone.angularai.testclient.model.UserDTO
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock

class ClearCommandTest {
    private lateinit var client: ApiClient
    private lateinit var clearCommand: ClearCommand

    @BeforeEach
    fun setUp() {
        client = mock()
        clearCommand = ClearCommand()
    }

    @Test
    fun `test clear tasks`() {
        val tasks = arrayOf(TaskDTO(id = 1L, title = "Task 1"))
        `when`(client.get(eq("/api/tasks"), eq(Array<TaskDTO>::class.java))).thenReturn(tasks)
        
        clearCommand.execute(client, listOf("tasks"))
        
        verify(client).delete("/api/tasks/1")
    }

    @Test
    fun `test clear logs`() {
        clearCommand.execute(client, listOf("logs"))
        verify(client).delete("/api/admin/logs")
    }

    @Test
    fun `test clear users keep defaults`() {
        val users = arrayOf(
            UserDTO(id = 1L, firstName = "A", lastName = "U", login = "admin", email = "a@e.c"),
            UserDTO(id = 2L, firstName = "T", lastName = "U", login = "testuser", email = "t@e.c")
        )
        `when`(client.get(eq("/api/admin/users"), eq(Array<UserDTO>::class.java))).thenReturn(users)
        
        clearCommand.execute(client, listOf("users", "--keep-defaults"))
        
        verify(client, never()).delete("/api/admin/users/1")
        verify(client).delete("/api/admin/users/2")
    }

    @Test
    fun `test clear all`() {
        `when`(client.get(eq("/api/tasks"), eq(Array<TaskDTO>::class.java))).thenReturn(emptyArray())
        `when`(client.get(eq("/api/admin/users"), eq(Array<UserDTO>::class.java))).thenReturn(emptyArray())
        
        clearCommand.execute(client, listOf("all"))
        
        verify(client).delete("/api/admin/logs")
        verify(client, atLeastOnce()).get(any(), any<Class<*>>())
    }
}
