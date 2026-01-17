package ch.goodone.angularai.backend.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void testUserEntity() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("First");
        user.setLastName("Last");
        user.setLogin("login");
        user.setPassword("pass");
        user.setEmail("email@ex.com");
        user.setBirthDate(LocalDate.now());
        user.setAddress("Addr");
        user.setRole(Role.ROLE_USER);
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);

        assertEquals(1L, user.getId());
        assertEquals("First", user.getFirstName());
        assertEquals("Last", user.getLastName());
        assertEquals("login", user.getLogin());
        assertEquals("pass", user.getPassword());
        assertEquals("email@ex.com", user.getEmail());
        assertNotNull(user.getBirthDate());
        assertEquals("Addr", user.getAddress());
        assertEquals(Role.ROLE_USER, user.getRole());
        assertEquals(now, user.getCreatedAt());

        user.onCreate(); // Trigger PrePersist logic
        assertNotNull(user.getCreatedAt());
    }

    @Test
    void testTaskEntity() {
        User user = new User();
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Title");
        task.setDescription("Desc");
        task.setDueDate(LocalDate.now());
        task.setPriority(Priority.HIGH);
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setPosition(1);
        task.setUser(user);
        LocalDateTime now = LocalDateTime.now();
        task.setCreatedAt(now);

        assertEquals(1L, task.getId());
        assertEquals("Title", task.getTitle());
        assertEquals("Desc", task.getDescription());
        assertNotNull(task.getDueDate());
        assertEquals(Priority.HIGH, task.getPriority());
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        assertEquals(1, task.getPosition());
        assertEquals(user, task.getUser());
        assertEquals(now, task.getCreatedAt());

        task.onCreate();
        assertNotNull(task.getCreatedAt());
    }

    @Test
    void testUserEntityConstructors() {
        User user = new User("First", "Last", "login", "pass", "email", LocalDate.now(), "Addr", Role.ROLE_USER);
        assertEquals("First", user.getFirstName());
        assertEquals(Role.ROLE_USER, user.getRole());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    void testTaskEntityConstructors() {
        User user = new User();
        Task task = new Task("Title", "Desc", LocalDate.now(), Priority.HIGH, user);
        assertEquals("Title", task.getTitle());
        assertEquals(user, task.getUser());
        assertEquals(TaskStatus.OPEN, task.getStatus());
        assertNotNull(task.getCreatedAt());
    }

    @Test
    void testActionLogEntityConstructors() {
        ActionLog log = new ActionLog("login", "action", "details");
        assertEquals("login", log.getLogin());
        assertNotNull(log.getTimestamp());

        LocalDateTime now = LocalDateTime.now();
        ActionLog log2 = new ActionLog("login", "action", "details", now);
        assertEquals(now, log2.getTimestamp());
    }
}
