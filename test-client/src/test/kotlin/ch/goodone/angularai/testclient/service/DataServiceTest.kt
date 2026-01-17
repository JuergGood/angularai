package ch.goodone.angularai.testclient.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue

class DataServiceTest {

    @Test
    fun `test createSampleTasks`() {
        val tasks = DataService.createSampleTasks()
        assertTrue(tasks.isNotEmpty())
        assertEquals(6, tasks.size)
    }

    @Test
    fun `test createPagingTasks`() {
        val tasks = DataService.createPagingTasks(10)
        assertEquals(10, tasks.size)
        assertEquals("Paging Task #1", tasks[0].title)
    }

    @Test
    fun `test createPagingLogs`() {
        val logs = DataService.createPagingLogs(5)
        assertEquals(5, logs.size)
        assertNotNull(logs[0].timestamp)
    }

    @Test
    fun `test createSampleUsers`() {
        val users = DataService.createSampleUsers()
        assertEquals(4, users.size)
        assertEquals("testuser", users[0].login)
    }

    @Test
    fun `test createSampleLogs`() {
        val logs = DataService.createSampleLogs()
        assertEquals(60, logs.size)
    }
}
