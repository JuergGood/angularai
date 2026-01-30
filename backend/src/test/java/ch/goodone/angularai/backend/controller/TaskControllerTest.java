package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.dto.TaskDTO;
import ch.goodone.angularai.backend.model.Priority;
import ch.goodone.angularai.backend.model.Role;
import ch.goodone.angularai.backend.model.User;
import ch.goodone.angularai.backend.repository.UserRepository;
import ch.goodone.angularai.backend.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ch.goodone.angularai.backend.config.SecurityConfig;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@org.springframework.test.context.TestPropertySource(locations = "classpath:test-common.properties")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class TaskControllerTest {

    @MockitoBean
    private org.springframework.mail.javamail.JavaMailSender javaMailSender;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ch.goodone.angularai.backend.service.TaskParserService taskParserService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private TaskDTO testTaskDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        testUser = new User("testuser", "test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("password");
        testUser.setBirthDate(LocalDate.now());
        testUser.setAddress("Address");
        testUser.setRole(Role.ROLE_USER);
        testUser.setId(1L);

        testTaskDTO = new TaskDTO(1L, "Test Task", "Description", LocalDate.now(), Priority.MEDIUM, "OPEN", 0);

        when(userRepository.findByLogin("testuser")).thenReturn(Optional.of(testUser));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldGetTasks() throws Exception {
        when(taskService.getTasks(testUser, null, null, null)).thenReturn(Collections.singletonList(testTaskDTO));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Task"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldGetTasksWithSmartFilter() throws Exception {
        when(taskService.getTasks(testUser, null, "TODAY", null)).thenReturn(Collections.singletonList(testTaskDTO));

        mockMvc.perform(get("/api/tasks?smartFilter=TODAY"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldPatchTask() throws Exception {
        TaskDTO patchDTO = new TaskDTO();
        patchDTO.setStatus("DONE");
        patchDTO.setTitle("New Title");

        TaskDTO resultDTO = new TaskDTO(1L, "New Title", "Description", LocalDate.now(), Priority.MEDIUM, "DONE", 0);
        
        when(taskService.patchTask(any(), eq(1L), any())).thenReturn(Optional.of(resultDTO));

        mockMvc.perform(patch("/api/tasks/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.title").value("New Title"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldReturnNotFoundWhenPatchingNonExistentTask() throws Exception {
        when(taskService.patchTask(any(), any(), any())).thenReturn(Optional.empty());
        mockMvc.perform(patch("/api/tasks/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldCreateTask() throws Exception {
        TaskDTO inputDTO = new TaskDTO(null, "New Task", "Desc", LocalDate.now(), Priority.HIGH, "OPEN", 0);
        when(taskService.createTask(any(), any())).thenReturn(testTaskDTO);

        mockMvc.perform(post("/api/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldDeleteTask() throws Exception {
        when(taskService.deleteTask(testUser, 1L)).thenReturn(true);

        mockMvc.perform(delete("/api/tasks/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldReorderTasks() throws Exception {
        java.util.List<Long> taskIds = java.util.Arrays.asList(1L, 2L);

        mockMvc.perform(put("/api/tasks/reorder")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskIds)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldAnalyzeTask() throws Exception {
        var parsed = new ch.goodone.angularai.backend.service.TaskParserService.ParsedTask(
                "Analyzed Task", "Desc", LocalDate.now(), Priority.HIGH, ch.goodone.angularai.backend.model.TaskStatus.OPEN, java.util.List.of()
        );
        when(taskParserService.parse(any())).thenReturn(parsed);

        mockMvc.perform(post("/api/tasks/analyze")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("some input"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Analyzed Task"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldBulkDeleteTasks() throws Exception {
        mockMvc.perform(delete("/api/tasks/bulk")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singletonList(1L))))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldUpdateTask() throws Exception {
        TaskDTO updateDTO = new TaskDTO();
        updateDTO.setTitle("Updated Title");

        TaskDTO resultDTO = new TaskDTO(1L, "Updated Title", "Description", LocalDate.now(), Priority.MEDIUM, "OPEN", 0);

        when(taskService.updateTask(any(), any(), any())).thenReturn(Optional.of(resultDTO));

        mockMvc.perform(put("/api/tasks/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldHandleBulkPatchTasks() throws Exception {
        TaskDTO patch = new TaskDTO();
        patch.setStatus("ARCHIVED");

        when(taskService.bulkPatchTasks(any(), any(), any())).thenReturn(Collections.singletonList(testTaskDTO));

        mockMvc.perform(patch("/api/tasks/bulk")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TaskController.BulkPatchRequest(java.util.List.of(1L), patch))))
                .andExpect(status().isOk());
    }
}
