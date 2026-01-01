package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.config.SecurityConfig;
import ch.goodone.angularai.backend.dto.UserDTO;
import ch.goodone.angularai.backend.model.User;
import ch.goodone.angularai.backend.repository.UserRepository;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getCurrentUser_shouldReturnUser_whenAuthenticated() throws Exception {
        String login = "testuser";
        String password = "password";
        User user = new User("Test", "User", login, passwordEncoder.encode(password), LocalDate.of(1990, 1, 1), "Address");
        
        when(userRepository.findByLogin(login)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/me")
                        .with(httpBasic(login, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value(login))
                .andExpect(jsonPath("$.firstName").value("Test"));
    }

    @Test
    void updateCurrentUser_shouldUpdateAndReturnUser() throws Exception {
        String login = "testuser";
        String password = "password";
        User user = new User("Test", "User", login, passwordEncoder.encode(password), LocalDate.of(1990, 1, 1), "Address");
        
        when(userRepository.findByLogin(login)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDTO updateDTO = new UserDTO(null, "Updated", "Name", login, LocalDate.of(1995, 5, 5), "New Address");

        mockMvc.perform(put("/api/users/me")
                        .with(httpBasic(login, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.address").value("New Address"));
    }
}
