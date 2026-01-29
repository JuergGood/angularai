package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.config.SecurityConfig;
import ch.goodone.angularai.backend.dto.UserDTO;
import ch.goodone.angularai.backend.model.Role;
import ch.goodone.angularai.backend.model.User;
import ch.goodone.angularai.backend.repository.UserRepository;
import ch.goodone.angularai.backend.service.ActionLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ActionLogService actionLogService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getCurrentUser_shouldReturnUser_whenAuthenticated() throws Exception {
        String login = "testuser";
        String password = "password";
        User user = new User("Test", "User", login, passwordEncoder.encode(password), "test@example.com", LocalDate.of(1990, 1, 1), "Address", Role.ROLE_USER);
        
        when(userRepository.findByLogin(login)).thenReturn(Optional.of(user));
        
        mockMvc.perform(get("/api/users/me")
                        .with(httpBasic(login, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value(login))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void updateCurrentUser_shouldUpdateAndReturnUser() throws Exception {
        String login = "testuser";
        String password = "password";
        User user = new User("Test", "User", login, passwordEncoder.encode(password), "test@example.com", LocalDate.of(1990, 1, 1), "Address", Role.ROLE_USER);
        
        when(userRepository.findByLogin(login)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDTO updateDTO = new UserDTO(null, "Updated", "Name", login, "updated@example.com", LocalDate.of(1995, 5, 5), "New Address", "ROLE_USER");

        mockMvc.perform(put("/api/users/me")
                        .with(csrf())
                        .with(httpBasic(login, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.address").value("New Address"));
    }

    @Test
    void deleteCurrentUser_shouldDelete_whenUserIsDeletable() throws Exception {
        String login = "deletableuser";
        String password = "password";
        User user = new User("Deletable", "User", login, passwordEncoder.encode(password), "delete@example.com", LocalDate.of(1990, 1, 1), "Address", Role.ROLE_USER);

        when(userRepository.findByLogin(login)).thenReturn(Optional.of(user));

        mockMvc.perform(delete("/api/users/me")
                        .with(csrf())
                        .with(httpBasic(login, password)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCurrentUser_shouldReturnForbidden_whenUserIsAdmin() throws Exception {
        String login = "admin";
        String password = "password";
        User user = new User("Admin", "User", login, passwordEncoder.encode(password), "admin@example.com", LocalDate.of(1990, 1, 1), "Address", Role.ROLE_ADMIN);

        when(userRepository.findByLogin(login)).thenReturn(Optional.of(user));

        mockMvc.perform(delete("/api/users/me")
                        .with(csrf())
                        .with(httpBasic(login, password)))
                .andExpect(status().isForbidden());
    }
}
