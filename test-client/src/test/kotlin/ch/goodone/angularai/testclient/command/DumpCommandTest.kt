package ch.goodone.angularai.testclient.command

import ch.goodone.angularai.testclient.client.ApiClient
import ch.goodone.angularai.testclient.model.PageResponse
import ch.goodone.angularai.testclient.model.TaskDTO
import ch.goodone.angularai.testclient.model.UserDTO
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import java.io.File

class DumpCommandTest {
    private lateinit var client: ApiClient
    private lateinit var dumpCommand: DumpCommand

    @BeforeEach
    fun setUp() {
        client = mock()
        dumpCommand = DumpCommand()
    }

    @Test
    fun `test dump all`() {
        `when`(client.get(eq("/api/tasks"), eq(Array<TaskDTO>::class.java))).thenReturn(emptyArray())
        `when`(client.get(eq("/api/admin/users"), eq(Array<UserDTO>::class.java))).thenReturn(emptyArray())
        
        val pageResponse = PageResponse<Any>(content = emptyList(), totalElements = 0, totalPages = 0, size = 10, number = 0)
        `when`(client.get(eq("/api/admin/logs?size=1000"), eq(PageResponse::class.java))).thenReturn(pageResponse)

        dumpCommand.execute(client, emptyList())

        verify(client).get(eq("/api/tasks"), eq(Array<TaskDTO>::class.java))
        verify(client).get(eq("/api/admin/users"), eq(Array<UserDTO>::class.java))
        verify(client).get(eq("/api/admin/logs?size=1000"), eq(PageResponse::class.java))

        // Cleanup
        File("dump_tasks.json").delete()
        File("dump_users.json").delete()
        File("dump_logs.json").delete()
    }
}
