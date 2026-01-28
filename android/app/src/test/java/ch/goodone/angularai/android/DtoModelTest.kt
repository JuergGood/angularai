package ch.goodone.angularai.android

import ch.goodone.angularai.android.data.remote.dto.*
import ch.goodone.angularai.android.data.local.entity.TaskEntity
import org.junit.Test
import org.junit.Assert.*

class DtoModelTest {
    @Test
    fun actionLogDTO_shouldStoreData() {
        val dto = ActionLogDTO(1L, "2024-01-01", "user", "ACTION", "Details")
        assertEquals(1L, dto.id)
        assertEquals("2024-01-01", dto.timestamp)
        assertEquals("user", dto.login)
        assertEquals("ACTION", dto.action)
        assertEquals("Details", dto.details)
    }

    @Test
    fun logResponseDTO_shouldStoreData() {
        val log = ActionLogDTO(1L, "2024-01-01", "user", "ACTION", "Details")
        val dto = LogResponseDTO(listOf(log), 1L, 1, 10, 0)
        assertEquals(1, dto.content.size)
        assertEquals(1L, dto.totalElements)
        assertEquals(1, dto.totalPages)
        assertEquals(10, dto.size)
        assertEquals(0, dto.number)
    }

    @Test
    fun dashboardDTO_shouldStoreData() {
        val summary = SummaryStatsDTO(1, 1, 1, 1, 1, 1, 1, 1)
        val distribution = TaskStatusDistributionDTO(1, 1, 1, 3)
        val dto = DashboardDTO(summary, emptyList(), emptyList(), emptyList(), distribution)
        
        assertEquals(summary, dto.summary)
        assertEquals(distribution, dto.taskDistribution)
        assertTrue(dto.priorityTasks.isEmpty())
        assertTrue(dto.recentActivity.isEmpty())
        assertTrue(dto.recentUsers.isEmpty())
    }

    @Test
    fun systemInfoDTO_shouldStoreData() {
        val dto = SystemInfoDTO("1.0", "1.0", "Dev", "Message")
        assertEquals("1.0", dto.backendVersion)
        assertEquals("1.0", dto.frontendVersion)
        assertEquals("Dev", dto.mode)
        assertEquals("Message", dto.landingMessage)
    }

    @Test
    fun taskDTO_shouldStoreData() {
        val dto = TaskDTO(1L, "Title", "Desc", "2024-01-01", "HIGH", "OPEN", 0, "2024-01-01")
        assertEquals(1L, dto.id)
        assertEquals("Title", dto.title)
        assertEquals("Desc", dto.description)
        assertEquals("2024-01-01", dto.dueDate)
        assertEquals("HIGH", dto.priority)
        assertEquals("OPEN", dto.status)
        assertEquals(0, dto.position)
        assertEquals("2024-01-01", dto.createdAt)
    }

    @Test
    fun userDTO_shouldStoreData() {
        val dto = UserDTO(1L, "First", "Last", "login", "email", "2000-01-01", "Addr", "USER", "pass", "2024-01-01")
        assertEquals(1L, dto.id)
        assertEquals("First", dto.firstName)
        assertEquals("Last", dto.lastName)
        assertEquals("login", dto.login)
        assertEquals("email", dto.email)
        assertEquals("2000-01-01", dto.birthDate)
        assertEquals("Addr", dto.address)
        assertEquals("USER", dto.role)
        assertEquals("pass", dto.password)
        assertEquals("2024-01-01", dto.createdAt)
    }

    @Test
    fun taskEntity_shouldStoreData() {
        val entity = TaskEntity(1L, "Title", "Desc", "2024-01-01", "HIGH", "OPEN", 0)
        assertEquals(1L, entity.id)
        assertEquals("Title", entity.title)
        assertEquals("Desc", entity.description)
        assertEquals("2024-01-01", entity.dueDate)
        assertEquals("HIGH", entity.priority)
        assertEquals("OPEN", entity.status)
        assertEquals(0, entity.position)
    }
}
