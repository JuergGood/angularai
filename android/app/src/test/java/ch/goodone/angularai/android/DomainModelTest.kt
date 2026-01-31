package ch.goodone.angularai.android

import ch.goodone.angularai.android.domain.model.User
import ch.goodone.angularai.android.domain.model.Task
import ch.goodone.angularai.android.domain.model.TaskStatus
import org.junit.Test
import org.junit.Assert.*

class DomainModelTest {
    @Test
    fun userModel_shouldStoreData() {
        val user = User(id = 1, firstName = "Test", lastName = "User", login = "test", email = "test@example.com", address = "Addr", role = "USER")
        assertEquals("Test", user.firstName)
        assertEquals("User", user.lastName)
        assertEquals("test", user.login)
        assertEquals("test@example.com", user.email)
        assertEquals("Addr", user.address)
        assertEquals("USER", user.role)
    }

    @Test
    fun taskModel_shouldStoreData() {
        val task = Task(id = 1, title = "Test Task", description = "Desc", dueDate = "2024-01-01", priority = "HIGH", status = TaskStatus.OPEN, position = 0)
        assertEquals("Test Task", task.title)
        assertEquals("Desc", task.description)
        assertEquals("2024-01-01", task.dueDate)
        assertEquals("HIGH", task.priority)
        assertEquals(TaskStatus.OPEN, task.status)
        assertEquals(0, task.position)
    }

    @Test
    fun taskStatus_shouldHaveCorrectValues() {
        assertEquals(4, TaskStatus.values().size)
        assertTrue(TaskStatus.valueOf("OPEN") == TaskStatus.OPEN)
        assertTrue(TaskStatus.valueOf("IN_PROGRESS") == TaskStatus.IN_PROGRESS)
        assertTrue(TaskStatus.valueOf("COMPLETED") == TaskStatus.COMPLETED)
        assertTrue(TaskStatus.valueOf("CLOSED") == TaskStatus.CLOSED)
    }
    @Test
    fun taskStatus_shouldFormatCorrectly() {
        val testCases = mapOf(
            TaskStatus.OPEN to "Open",
            TaskStatus.IN_PROGRESS to "In Progress",
            TaskStatus.COMPLETED to "Completed",
            TaskStatus.CLOSED to "Closed"
        )
        
        testCases.forEach { (status, expected) ->
            val formatted = status.name.lowercase().replace("_", " ").split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
            assertEquals(expected, formatted)
        }
    }
}
