package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.config.SecurityConfig;
import ch.goodone.angularai.backend.dto.UserDTO;
import ch.goodone.angularai.backend.model.Role;
import ch.goodone.angularai.backend.model.User;
import ch.goodone.angularai.backend.repository.UserRepository;
import ch.goodone.angularai.backend.service.ActionLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ActionLogService actionLogService;

    @Autowired
    private ObjectMapper objectMapper;

    private User adminUser;
    private User normalUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        adminUser = new User("Admin", "User", "admin", "password", "admin@example.com", LocalDate.now(), "Admin Address", Role.ROLE_ADMIN);
        adminUser.setId(1L);

        normalUser = new User("Normal", "User", "user", "password", "user@example.com", LocalDate.now(), "User Address", Role.ROLE_USER);
        normalUser.setId(2L);

        when(userRepository.findByLogin("admin")).thenReturn(Optional.of(adminUser));
        when(userRepository.findByLogin("user")).thenReturn(Optional.of(normalUser));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void shouldGetAllUsers() throws Exception {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(normalUser));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].login").value("user"));
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    void shouldDenyAccessToNonAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void shouldUpdateUserRole() throws Exception {
        UserDTO updateDTO = new UserDTO(2L, "Normal", "User", "user", "user@example.com", LocalDate.now(), "User Address", "ROLE_ADMIN");
        
        when(userRepository.findById(2L)).thenReturn(Optional.of(normalUser));
        when(userRepository.findAll()).thenReturn(Collections.singletonList(normalUser));

        mockMvc.perform(put("/api/admin/users/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
        
        verify(userRepository).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void shouldDeleteUser() throws Exception {
        when(userRepository.findById(2L)).thenReturn(Optional.of(normalUser));

        mockMvc.perform(delete("/api/admin/users/2"))
                .andExpect(status().isNoContent());

        verify(userRepository).delete(normalUser);
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void shouldNotDeleteSelf() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        mockMvc.perform(delete("/api/admin/users/1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cannot delete your own account"));
    }

    @Test
    @WithMockUser(username = "adminread", authorities = {"ROLE_ADMIN_READ"})
    void adminReadShouldSeeUsers() throws Exception {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(normalUser));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].login").value("user"))
                .andExpect(jsonPath("$[0].role").value("ROLE_USER"));
    }

    @Test
    @WithMockUser(username = "adminread", authorities = {"ROLE_ADMIN_READ"})
    void adminReadShouldNotUpdateUser() throws Exception {
        UserDTO updateDTO = new UserDTO(2L, "Normal", "User", "user", "user@example.com", LocalDate.now(), "User Address", "ROLE_ADMIN");

        mockMvc.perform(put("/api/admin/users/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "adminread", authorities = {"ROLE_ADMIN_READ"})
    void adminReadShouldNotDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/admin/users/2"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void shouldNotUpdateAdminOwnRole() throws Exception {
        UserDTO updateDTO = new UserDTO(1L, "Admin", "User", "admin", "admin@example.com", LocalDate.now(), "Admin Address", "ROLE_USER");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        mockMvc.perform(put("/api/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cannot remove your own admin role"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void shouldReturnBadRequest_whenCreateUserWithDuplicateLogin() throws Exception {
        UserDTO userDTO = new UserDTO(null, "New", "User", "admin", "new@example.com", LocalDate.now(), "Addr", "ROLE_USER");
        when(userRepository.findByLogin("admin")).thenReturn(Optional.of(adminUser));

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Login already exists"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void shouldReturnBadRequest_whenCreateUserWithDuplicateEmail() throws Exception {
        UserDTO userDTO = new UserDTO(null, "New", "User", "newuser", "admin@example.com", LocalDate.now(), "Addr", "ROLE_USER");
        when(userRepository.findByLogin("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email already exists"));
    }
}
