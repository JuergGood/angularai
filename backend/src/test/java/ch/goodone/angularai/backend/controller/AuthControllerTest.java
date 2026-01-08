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

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ActionLogService actionLogService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void login_shouldReturnUser_whenAuthenticated() throws Exception {
        String login = "admin";
        String password = "password";
        User user = new User("Admin", "User", login, passwordEncoder.encode(password), "admin@example.com", LocalDate.of(1980, 1, 1), "Admin Home", Role.ROLE_ADMIN);
        
        when(userRepository.findByLogin(login)).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/auth/login")
                        .with(httpBasic(login, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value(login))
                .andExpect(jsonPath("$.firstName").value("Admin"))
                .andExpect(jsonPath("$.email").value("admin@example.com"));
    }

    @Test
    void register_shouldCreateUser() throws Exception {
        UserDTO userDTO = new UserDTO(null, "New", "User", "newuser", "new@example.com", LocalDate.of(2000, 1, 1), "New Address", "ROLE_USER");
        userDTO.setPassword("password123");

        when(userRepository.findByLogin("newuser")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"New\",\"lastName\":\"User\",\"login\":\"newuser\",\"email\":\"new@example.com\",\"password\":\"password123\",\"birthDate\":\"2000-01-01\",\"address\":\"New Address\",\"role\":\"ROLE_USER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("newuser"))
                .andExpect(jsonPath("$.email").value("new@example.com"));
    }

    @Test
    void register_shouldReturnBadRequest_whenUserExists() throws Exception {
        UserDTO userDTO = new UserDTO(null, "Existing", "User", "admin", "admin@example.com", LocalDate.of(1990, 1, 1), "Address", "ROLE_USER");
        userDTO.setPassword("password123");
        
        when(userRepository.findByLogin("admin")).thenReturn(Optional.of(new User()));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Existing\",\"lastName\":\"User\",\"login\":\"admin\",\"email\":\"admin@example.com\",\"password\":\"password123\",\"birthDate\":\"1990-01-01\",\"address\":\"Address\",\"role\":\"ROLE_USER\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User already exists"));
    }

    @Test
    void register_shouldReturnBadRequest_whenEmailInvalid() throws Exception {
        UserDTO userDTO = new UserDTO(null, "New", "User", "newuser", "invalid-email", LocalDate.of(2000, 1, 1), "New Address", "ROLE_USER");
        userDTO.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"New\",\"lastName\":\"User\",\"login\":\"newuser\",\"email\":\"invalid-email\",\"password\":\"password123\",\"birthDate\":\"2000-01-01\",\"address\":\"New Address\",\"role\":\"ROLE_USER\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid email format"));
    }

    @Test
    void register_shouldReturnBadRequest_whenFirstNameMissing() throws Exception {
        UserDTO userDTO = new UserDTO(null, "", "User", "newuser", "new@example.com", LocalDate.of(2000, 1, 1), "New Address", "ROLE_USER");
        userDTO.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("First name is required"));
    }

    @Test
    void register_shouldReturnBadRequest_whenLastNameMissing() throws Exception {
        UserDTO userDTO = new UserDTO(null, "New", "", "newuser", "new@example.com", LocalDate.of(2000, 1, 1), "New Address", "ROLE_USER");
        userDTO.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Last name is required"));
    }

    @Test
    void register_shouldReturnBadRequest_whenLoginMissing() throws Exception {
        UserDTO userDTO = new UserDTO(null, "New", "User", "", "new@example.com", LocalDate.of(2000, 1, 1), "New Address", "ROLE_USER");
        userDTO.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Login is required"));
    }

    @Test
    void register_shouldReturnBadRequest_whenPasswordMissing() throws Exception {
        UserDTO userDTO = new UserDTO(null, "New", "User", "newuser", "new@example.com", LocalDate.of(2000, 1, 1), "New Address", "ROLE_USER");
        // No password set

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Password is required"));
    }

    @Test
    void register_shouldReturnBadRequest_whenEmailMissing() throws Exception {
        UserDTO userDTO = new UserDTO(null, "New", "User", "newuser", "", LocalDate.of(2000, 1, 1), "New Address", "ROLE_USER");
        userDTO.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"New\",\"lastName\":\"User\",\"login\":\"newuser\",\"email\":\"\",\"password\":\"password123\",\"birthDate\":\"2000-01-01\",\"address\":\"New Address\",\"role\":\"ROLE_USER\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email is required"));
    }

    @Test
    void register_shouldReturnBadRequest_whenBirthDateInvalid() throws Exception {
        String json = "{\"firstName\":\"New\",\"lastName\":\"User\",\"login\":\"newuser\",\"email\":\"new@example.com\",\"password\":\"password123\",\"birthDate\":\"invalid-date\"}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturnBadRequest_whenBirthDateEmpty() throws Exception {
        String json = "{\"firstName\":\"New\",\"lastName\":\"User\",\"login\":\"newuser\",\"email\":\"new@example.com\",\"password\":\"password123\",\"birthDate\":\"\"}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/auth/login"))
                .andExpect(status().isUnauthorized());
    }
}
