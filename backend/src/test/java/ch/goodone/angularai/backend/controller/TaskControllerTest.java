package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.dto.TaskDTO;
import ch.goodone.angularai.backend.model.Priority;
import ch.goodone.angularai.backend.model.Task;
import ch.goodone.angularai.backend.model.User;
import ch.goodone.angularai.backend.repository.TaskRepository;
import ch.goodone.angularai.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
@Import(SecurityConfig.class)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private TaskRepository taskRepository;

    @MockitoBean
    private UserRepository userRepository;

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

        testUser = new User("Test", "User", "testuser", "password", "test@example.com", LocalDate.now(), "Address");
        testUser.setId(1L);

        testTask = new Task("Test Task", "Description", LocalDate.now(), Priority.MEDIUM, testUser);
        testTask.setId(1L);

        when(userRepository.findByLogin("testuser")).thenReturn(Optional.of(testUser));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldGetTasks() throws Exception {
        when(taskRepository.findByUser(any())).thenReturn(Collections.singletonList(testTask));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Task"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldCreateTask() throws Exception {
        TaskDTO taskDTO = new TaskDTO(null, "New Task", "Desc", LocalDate.now(), Priority.HIGH);
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
        TaskDTO taskDTO = new TaskDTO(null, "No Date Task", "Desc", null, Priority.LOW);
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
    void shouldDeleteTask() throws Exception {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldNotDeleteOtherUserTask() throws Exception {
        User otherUser = new User();
        otherUser.setId(2L);
        Task otherTask = new Task();
        otherTask.setUser(otherUser);
        otherTask.setId(1L);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(otherTask));

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNotFound());
    }
}
