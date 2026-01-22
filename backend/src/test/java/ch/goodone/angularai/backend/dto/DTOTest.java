package ch.goodone.angularai.backend.dto;

import ch.goodone.angularai.backend.model.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DTOTest {

    @Test
    void testUserDTO() {
        User user = new User("First", "Last", "login", "pass", "email@ex.com", LocalDate.now(), "Addr", Role.ROLE_USER);
        user.setId(1L);
        user.setCreatedAt(LocalDateTime.now());

        UserDTO dto = UserDTO.fromEntity(user);
        assertEquals(1L, dto.getId());
        assertEquals("First", dto.getFirstName());
        assertEquals("Last", dto.getLastName());
        assertEquals("login", dto.getLogin());
        assertEquals("email@ex.com", dto.getEmail());
        assertEquals("ROLE_USER", dto.getRole());
        assertNotNull(dto.getBirthDate());
        assertEquals("Addr", dto.getAddress());
        assertNotNull(dto.getCreatedAt());

        UserDTO manual = new UserDTO();
        manual.setId(2L);
        manual.setFirstName("F");
        manual.setLastName("L");
        manual.setLogin("log");
        manual.setPassword("p");
        manual.setEmail("e");
        manual.setBirthDate(LocalDate.now());
        manual.setAddress("A");
        manual.setRole("ROLE_ADMIN");
        manual.setCreatedAt(LocalDateTime.now());

        assertEquals(2L, manual.getId());
        assertEquals("p", manual.getPassword());
    }

    @Test
    void testTaskDTO() {
        User user = new User();
        user.setId(1L);
        Task task = new Task("Title", "Desc", LocalDate.now(), Priority.HIGH, user);
        task.setId(1L);
        task.setStatus(TaskStatus.OPEN);
        task.setPosition(5);
        task.setCreatedAt(LocalDateTime.now());

        TaskDTO dto = TaskDTO.fromEntity(task);
        assertEquals(1L, dto.getId());
        assertEquals("Title", dto.getTitle());
        assertEquals("Desc", dto.getDescription());
        assertEquals(Priority.HIGH, dto.getPriority());
        assertEquals("OPEN", dto.getStatus());
        assertEquals(5, dto.getPosition());
        assertNotNull(dto.getCreatedAt());

        TaskDTO manual = new TaskDTO();
        manual.setTitle("T");
        manual.setDescription("D");
        manual.setDueDate(LocalDate.now());
        manual.setPriority(Priority.LOW);
        manual.setStatus("COMPLETED");
        manual.setPosition(2);
        manual.setCreatedAt(LocalDateTime.now());

        assertEquals("T", manual.getTitle());
    }

    @Test
    void testActionLogDTO() {
        ActionLog log = new ActionLog("login", "action", "details");
        log.setId(1L);
        log.setTimestamp(LocalDateTime.now());

        ActionLogDTO dto = ActionLogDTO.fromEntity(log);
        assertEquals(1L, dto.getId());
        assertEquals("login", dto.getLogin());
        assertEquals("action", dto.getAction());
        assertEquals("details", dto.getDetails());
        assertNotNull(dto.getTimestamp());

        ActionLogDTO manual = new ActionLogDTO();
        manual.setLogin("l");
        manual.setAction("a");
        manual.setDetails("d");
        manual.setTimestamp(LocalDateTime.now());
        assertEquals("l", manual.getLogin());
    }

    @Test
    void testDashboardDTO() {
        DashboardDTO.SummaryStats summary = new DashboardDTO.SummaryStats(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(1, summary.getOpenTasks());
        assertEquals(2, summary.getOpenTasksDelta());
        assertEquals(3, summary.getActiveUsers());
        assertEquals(4, summary.getActiveUsersDelta());
        assertEquals(5, summary.getCompletedTasks());
        assertEquals(6, summary.getCompletedTasksDelta());
        assertEquals(7, summary.getTodayLogs());
        assertEquals(8, summary.getTodayLogsDelta());

        DashboardDTO.TaskStatusDistribution dist = new DashboardDTO.TaskStatusDistribution(1, 2, 3, 0, 6);
        assertEquals(1, dist.getOpen());
        assertEquals(2, dist.getInProgress());
        assertEquals(3, dist.getCompleted());
        assertEquals(0, dist.getClosed());
        assertEquals(6, dist.getTotal());
        
        DashboardDTO dash = new DashboardDTO();
        dash.setSummary(summary);
        dash.setTaskDistribution(dist);
        assertNotNull(dash.getSummary());
        assertNotNull(dash.getTaskDistribution());
    }
}
