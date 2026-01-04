package ch.goodone.angularai.android

import ch.goodone.angularai.android.domain.model.User
import ch.goodone.angularai.android.domain.model.Task
import org.junit.Test
import org.junit.Assert.*

class DomainModelTest {
    @Test
    fun userModel_shouldStoreData() {
        val user = User(id = 1, firstName = "Test", lastName = "User", login = "test")
        assertEquals("Test", user.firstName)
        assertEquals("User", user.lastName)
        assertEquals("test", user.login)
    }

    @Test
    fun taskModel_shouldStoreData() {
        val task = Task(id = 1, title = "Test Task", description = "Desc", dueDate = "2024-01-01", priority = "HIGH")
        assertEquals("Test Task", task.title)
        assertEquals("HIGH", task.priority)
    }
}
