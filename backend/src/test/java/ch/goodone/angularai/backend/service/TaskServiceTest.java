package ch.goodone.angularai.backend.service;

import ch.goodone.angularai.backend.dto.TaskDTO;
import ch.goodone.angularai.backend.model.Priority;
import ch.goodone.angularai.backend.model.Task;
import ch.goodone.angularai.backend.model.TaskStatus;
import ch.goodone.angularai.backend.model.User;
import ch.goodone.angularai.backend.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ActionLogService actionLogService;

    @InjectMocks
    private TaskService taskService;

    private User testUser;
    private Task testTask;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com");
        testUser.setId(1L);

        testTask = new Task("Test Task", "Description", LocalDate.now(), Priority.MEDIUM, testUser);
        testTask.setId(100L);
        testTask.setStatus(TaskStatus.OPEN);
        testTask.setPosition(0);
    }

    @Test
    void getTasks_ShouldReturnTasks() {
        when(taskRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(testTask));

        List<TaskDTO> tasks = taskService.getTasks(testUser, null, null, null);

        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertEquals("Test Task", tasks.get(0).getTitle());
    }

    @Test
    void getTasks_WithFiltersAndSort() {
        when(taskRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(testTask));

        taskService.getTasks(testUser, "OPEN", "TODAY", "DUE_ASC");
        taskService.getTasks(testUser, "DONE", "UPCOMING", "PRIO_DESC");
        taskService.getTasks(testUser, "ARCHIVED", "OVERDUE", "UPDATED_DESC");
        taskService.getTasks(testUser, "IN_PROGRESS", "HIGH", "INVALID_SORT");
        taskService.getTasks(testUser, null, "ALL", null);
        taskService.getTasks(testUser, null, "INVALID_FILTER", null);

        verify(taskRepository, times(6)).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    void updateTask_ShouldReturnEmpty_WhenNotFound() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());
        Optional<TaskDTO> result = taskService.updateTask(testUser, 999L, new TaskDTO());
        assertFalse(result.isPresent());
    }

    @Test
    void updateTask_ShouldReturnEmpty_WhenUserMismatch() {
        User otherUser = new User("other", "other@example.com");
        otherUser.setId(2L);
        when(taskRepository.findById(100L)).thenReturn(Optional.of(testTask));
        
        Optional<TaskDTO> result = taskService.updateTask(otherUser, 100L, new TaskDTO());
        assertFalse(result.isPresent());
    }

    @Test
    void patchTask_ShouldReturnEmpty_WhenNotFound() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());
        Optional<TaskDTO> result = taskService.patchTask(testUser, 999L, new TaskDTO());
        assertFalse(result.isPresent());
    }

    @Test
    void deleteTask_ShouldReturnFalse_WhenNotFound() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());
        boolean result = taskService.deleteTask(testUser, 999L);
        assertFalse(result);
    }

    @Test
    void bulkPatchTasks_ShouldHandleUserMismatch() {
        User otherUser = new User("other", "other@example.com");
        otherUser.setId(2L);
        when(taskRepository.findAllById(any())).thenReturn(List.of(testTask));
        when(taskRepository.saveAll(anyList())).thenReturn(List.of());

        List<TaskDTO> results = taskService.bulkPatchTasks(otherUser, List.of(100L), new TaskDTO());
        assertTrue(results.isEmpty());
    }

    @Test
    void createTask_WithNullStatusAndTags() {
        when(taskRepository.findByUserOrderByPositionAsc(testUser)).thenReturn(List.of());
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        TaskDTO dto = new TaskDTO();
        dto.setTitle("No status task");
        dto.setStatus(null);
        dto.setTags(List.of("tag1", "tag2"));

        taskService.createTask(testUser, dto);
        verify(taskRepository).save(argThat(t -> t.getTags().size() == 2));
    }

    @Test
    void createTask_ShouldSaveTask() {
        when(taskRepository.findByUserOrderByPositionAsc(testUser)).thenReturn(new ArrayList<>());
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        TaskDTO dto = new TaskDTO();
        dto.setTitle("New Task");
        dto.setPriority(Priority.HIGH);
        dto.setStatus("OPEN");

        TaskDTO result = taskService.createTask(testUser, dto);

        assertNotNull(result);
        verify(taskRepository).save(any(Task.class));
        verify(actionLogService).log(eq("testuser"), eq("TASK_ADDED"), anyString());
    }

    @Test
    void updateTask_ShouldUpdateExistingTask() {
        when(taskRepository.findById(100L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        TaskDTO dto = new TaskDTO();
        dto.setTitle("Updated Title");
        dto.setPriority(Priority.CRITICAL);
        dto.setStatus("DONE");

        Optional<TaskDTO> result = taskService.updateTask(testUser, 100L, dto);

        assertTrue(result.isPresent());
        assertEquals("Updated Title", result.get().getTitle());
        verify(taskRepository).save(testTask);
        verify(actionLogService).log(eq("testuser"), eq("TASK_UPDATED"), anyString());
    }

    @Test
    void patchTask_ShouldPatchFields() {
        when(taskRepository.findById(100L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        TaskDTO dto = new TaskDTO();
        dto.setTitle("Patched Title");

        Optional<TaskDTO> result = taskService.patchTask(testUser, 100L, dto);

        assertTrue(result.isPresent());
        assertEquals("Patched Title", result.get().getTitle());
        verify(taskRepository).save(testTask);
    }

    @Test
    void bulkPatchTasks_ShouldUpdateMultipleTasks() {
        Task task2 = new Task("Task 2", "", null, Priority.LOW, testUser);
        task2.setId(101L);
        
        when(taskRepository.findAllById(any())).thenReturn(Arrays.asList(testTask, task2));
        when(taskRepository.saveAll(any())).thenReturn(Arrays.asList(testTask, task2));

        TaskDTO patch = new TaskDTO();
        patch.setStatus("DONE");
        patch.setPriority(Priority.HIGH);

        List<TaskDTO> results = taskService.bulkPatchTasks(testUser, Arrays.asList(100L, 101L), patch);

        assertEquals(2, results.size());
        verify(taskRepository).saveAll(any());
        verify(actionLogService).log(eq("testuser"), eq("TASK_BULK_PATCH"), anyString());
    }

    @Test
    void reorderTasks_ShouldUpdatePositions() {
        Task task2 = new Task("Task 2", "", null, Priority.LOW, testUser);
        task2.setId(101L);
        
        when(taskRepository.findByUserOrderByPositionAsc(testUser)).thenReturn(new ArrayList<>(Arrays.asList(testTask, task2)));

        taskService.reorderTasks(testUser, Arrays.asList(101L, 100L));

        verify(taskRepository).saveAll(any());
        assertEquals(1, testTask.getPosition());
        assertEquals(0, task2.getPosition());
    }

    @Test
    void deleteTask_ShouldRemoveTask() {
        when(taskRepository.findById(100L)).thenReturn(Optional.of(testTask));

        boolean result = taskService.deleteTask(testUser, 100L);

        assertTrue(result);
        verify(taskRepository).delete(testTask);
    }

    @Test
    void bulkDeleteTasks_ShouldRemoveMultipleTasks() {
        when(taskRepository.findAllById(any())).thenReturn(List.of(testTask));

        taskService.bulkDeleteTasks(testUser, List.of(100L));

        verify(taskRepository).deleteAll(any());
    }
}
