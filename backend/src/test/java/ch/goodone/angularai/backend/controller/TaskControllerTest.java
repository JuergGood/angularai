package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.dto.TaskDTO;
import ch.goodone.angularai.backend.model.Priority;
import ch.goodone.angularai.backend.model.Role;
import ch.goodone.angularai.backend.model.Task;
import ch.goodone.angularai.backend.model.User;
import ch.goodone.angularai.backend.repository.TaskRepository;
import ch.goodone.angularai.backend.repository.UserRepository;
import ch.goodone.angularai.backend.service.ActionLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ch.goodone.angularai.backend.config.SecurityConfig;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private TaskRepository taskRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ActionLogService actionLogService;

    @MockitoBean
    private ch.goodone.angularai.backend.service.TaskParserService taskParserService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Task testTask;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        testUser = new User("Test", "User", "testuser", "password", "test@example.com", LocalDate.now(), "Address", Role.ROLE_USER);
        testUser.setId(1L);

        testTask = new Task("Test Task", "Description", LocalDate.now(), Priority.MEDIUM, testUser);
        testTask.setId(1L);

        when(userRepository.findByLogin("testuser")).thenReturn(Optional.of(testUser));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldGetTasks() throws Exception {
        when(taskRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(Collections.singletonList(testTask));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Task"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldGetTasksWithSmartFilter() throws Exception {
        when(taskRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(Collections.singletonList(testTask));

        mockMvc.perform(get("/api/tasks?smartFilter=TODAY"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldPatchTask() throws Exception {
        TaskDTO patchDTO = new TaskDTO();
        patchDTO.setStatus("DONE");
        patchDTO.setTitle("New Title");
        patchDTO.setDescription("New Desc");
        patchDTO.setDueDate(LocalDate.now());
        patchDTO.setPriority(Priority.CRITICAL);
        patchDTO.setTags(java.util.List.of("Tag"));
        
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(patch("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.title").value("New Title"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldReturnNotFoundWhenPatchingNonExistentTask() throws Exception {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        mockMvc.perform(patch("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldReturnNotFoundWhenPatchingOtherUserTask() throws Exception {
        User otherUser = new User();
        otherUser.setId(2L);
        testTask.setUser(otherUser);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        mockMvc.perform(patch("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldCreateTask() throws Exception {
        TaskDTO taskDTO = new TaskDTO(null, "New Task", "Desc", LocalDate.now(), Priority.HIGH, "OPEN", 0);
        when(taskRepository.save(any())).thenReturn(testTask);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldCreateTaskWithNullDueDate() throws Exception {
        TaskDTO taskDTO = new TaskDTO(null, "No Date Task", "Desc", null, Priority.LOW, "OPEN", 0);
        Task savedTask = new Task("No Date Task", "Desc", null, Priority.LOW, testUser);
        savedTask.setId(2L);
        when(taskRepository.save(any())).thenReturn(savedTask);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("No Date Task"))
                .andExpect(jsonPath("$.dueDate").isEmpty());
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldCreateTaskWithDoneStatus() throws Exception {
        TaskDTO taskDTO = new TaskDTO(null, "Done Task", "Desc", null, Priority.LOW, "DONE", 0);
        Task savedTask = new Task("Done Task", "Desc", null, Priority.LOW, testUser);
        savedTask.setStatus(ch.goodone.angularai.backend.model.TaskStatus.DONE);
        savedTask.setId(3L);
        when(taskRepository.save(any())).thenReturn(savedTask);
        
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldDeleteTask() throws Exception {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldReorderTasks() throws Exception {
        java.util.List<Long> taskIds = java.util.Arrays.asList(1L, 2L);
        when(taskRepository.findByUserOrderByPositionAsc(any())).thenReturn(Collections.singletonList(testTask));

        mockMvc.perform(put("/api/tasks/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskIds)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldReturnNotFoundWhenDeletingNonExistentTask() throws Exception {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldReturnNotFoundWhenDeletingOtherUserTask() throws Exception {
        User otherUser = new User();
        otherUser.setId(2L);
        testTask.setUser(otherUser);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldAnalyzeTask() throws Exception {
        var parsed = new ch.goodone.angularai.backend.service.TaskParserService.ParsedTask(
                "Analyzed Task", "Desc", LocalDate.now(), Priority.HIGH, ch.goodone.angularai.backend.model.TaskStatus.OPEN, java.util.List.of()
        );
        when(taskParserService.parse(any())).thenReturn(parsed);

        mockMvc.perform(post("/api/tasks/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content("some input"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Analyzed Task"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldBulkDeleteTasks() throws Exception {
        when(taskRepository.findAllById(any())).thenReturn(Collections.singletonList(testTask));

        mockMvc.perform(delete("/api/tasks/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singletonList(1L))))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldUpdateTask() throws Exception {
        TaskDTO updateDTO = new TaskDTO();
        updateDTO.setTitle("Updated Title");
        updateDTO.setStatus("IN_PROGRESS");
        updateDTO.setPriority(Priority.HIGH);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldHandleBulkPatchTasks() throws Exception {
        TaskDTO patch = new TaskDTO();
        patch.setTitle("Bulk Title");
        patch.setDescription("Bulk Desc");
        patch.setDueDate(LocalDate.now());
        patch.setPriority(Priority.LOW);
        patch.setTags(java.util.List.of("BulkTag"));
        patch.setStatus("ARCHIVED");

        String json = objectMapper.writeValueAsString(new TaskController.BulkPatchRequest(java.util.List.of(1L), patch));

        when(taskRepository.findAllById(any())).thenReturn(Collections.singletonList(testTask));
        when(taskRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(patch("/api/tasks/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ARCHIVED"))
                .andExpect(jsonPath("$[0].title").value("Bulk Title"));
    }
}
