package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.config.SecurityConfig;
import ch.goodone.angularai.backend.dto.UserDTO;
import ch.goodone.angularai.backend.model.PasswordRecoveryToken;
import ch.goodone.angularai.backend.model.Role;
import ch.goodone.angularai.backend.model.User;
import ch.goodone.angularai.backend.model.VerificationToken;
import ch.goodone.angularai.backend.repository.PasswordRecoveryTokenRepository;
import ch.goodone.angularai.backend.repository.UserRepository;
import ch.goodone.angularai.backend.repository.VerificationTokenRepository;
import ch.goodone.angularai.backend.service.ActionLogService;
import ch.goodone.angularai.backend.service.CaptchaService;
import ch.goodone.angularai.backend.service.EmailService;
import ch.goodone.angularai.backend.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@org.springframework.test.context.TestPropertySource(locations = "classpath:test-common.properties")
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ActionLogService actionLogService;

    @MockitoBean
    private CaptchaService captchaService;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private VerificationTokenRepository verificationTokenRepository;

    @MockitoBean
    private PasswordRecoveryTokenRepository passwordRecoveryTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        when(captchaService.verify(anyString())).thenReturn(true);
        when(captchaService.verify(null)).thenReturn(true);
    }

    @Test
    void register_shouldReturnBadRequest_whenUserAlreadyExists() throws Exception {
        UserDTO userDTO = new UserDTO(null, "Existing", "User", "admin", "admin@example.com", "123456", "ACTIVE", LocalDate.of(1990, 1, 1), "Address", "ROLE_USER");
        userDTO.setPassword("Password@123");
        userDTO.setRecaptchaToken("token");

        when(userRepository.findByLogin("admin")).thenReturn(Optional.of(new User("admin", "admin@example.com")));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void info_shouldReturnOkWithNull_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/info"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void forgotPassword_shouldAlwaysReturnOk() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nonexistent@example.com\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void verify_shouldConfirmEmailChange_whenPendingEmailExists() throws Exception {
        User user = new User("admin", "old@example.com");
        user.setPendingEmail("new@example.com");
        VerificationToken token = new VerificationToken(user);

        when(verificationTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(userRepository.save(any(User.class))).thenReturn(user);

        mockMvc.perform(get("/api/auth/verify")
                        .param("token", "valid-token"))
                .andExpect(status().isOk());

        verify(userRepository).save(argThat(u -> 
            "new@example.com".equals(u.getEmail()) && u.getPendingEmail() == null
        ));
    }

    @Test
    void resetPassword_shouldReturnBadRequest_whenTokenMissing() throws Exception {
        mockMvc.perform(post("/api/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"NewPassword!123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cleanupPendingUser_shouldBeCalledOnRegistration() throws Exception {
        UserDTO userDTO = new UserDTO(null, "FirstName", "LastName", "pendinguser", "pending@example.com", "123456", "PENDING", LocalDate.of(2000, 1, 1), "Address", "ROLE_USER");
        userDTO.setPassword("Password@123");
        userDTO.setRecaptchaToken("token");

        // Set up specific mocks for the validation helper methods
        when(userRepository.findByLogin("pendinguser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("pending@example.com")).thenReturn(Optional.empty());
        when(captchaService.verify("token")).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"FirstName\",\"lastName\":\"LastName\",\"login\":\"pendinguser\",\"email\":\"pending@example.com\",\"password\":\"Password@123\",\"recaptchaToken\":\"token\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void login_shouldReturnUser_whenAuthenticated() throws Exception {
        String login = "admin";
        String password = "password";
        User user = new User(login, "admin@example.com");
        user.setFirstName("Admin");
        user.setLastName("User");
        user.setPassword(passwordEncoder.encode(password));
        user.setPhone("+41791234567");
        user.setBirthDate(LocalDate.of(1980, 1, 1));
        user.setAddress("Admin Home");
        user.setRole(Role.ROLE_ADMIN);
        user.setStatus(ch.goodone.angularai.backend.model.UserStatus.ACTIVE);
        
        when(userRepository.findByLogin(login)).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .with(httpBasic(login, password))
                        .header("X-Forwarded-For", "1.2.3.4")
                        .header("User-Agent", "Mozilla/5.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value(login))
                .andExpect(jsonPath("$.firstName").value("Admin"))
                .andExpect(jsonPath("$.email").value("admin@example.com"));

        verify(actionLogService).logLogin(login, "1.2.3.4", "Mozilla/5.0");
    }

    @Test
    void register_shouldCreateUser() throws Exception {
        UserDTO userDTO = new UserDTO(null, "New", "User", "newuser", "new@example.com", "123456", "PENDING", LocalDate.of(2000, 1, 1), "New Address", "ROLE_USER");
        userDTO.setPassword("Password@123");

        when(userRepository.findByLogin("newuser")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"New\",\"lastName\":\"User\",\"login\":\"newuser\",\"email\":\"new@example.com\",\"phone\":\"123456\",\"password\":\"Password@123\",\"birthDate\":\"2000-01-01\",\"address\":\"New Address\",\"role\":\"ROLE_USER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("newuser"))
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN_READ"));
    }

    @Test
    void register_shouldCreateUser_withoutBirthDate() throws Exception {
        String json = "{\"firstName\":\"New\",\"lastName\":\"User\",\"login\":\"newuser\",\"email\":\"new@example.com\",\"phone\":\"123456\",\"password\":\"Password@123\",\"recaptchaToken\":\"test-token\",\"address\":\"New Address\",\"role\":\"ROLE_USER\"}";

        when(userRepository.findByLogin("newuser")).thenReturn(Optional.empty());
        when(captchaService.verify("test-token")).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("newuser"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN_READ"));
    }

    @Test
    void register_shouldReturnBadRequest_whenUserExists() throws Exception {
        UserDTO userDTO = new UserDTO(null, "Existing", "User", "admin", "admin@example.com", "123456", "PENDING", LocalDate.of(1990, 1, 1), "Address", "ROLE_USER");
        userDTO.setPassword("Password@123");
        
        when(userRepository.findByLogin("admin")).thenReturn(Optional.of(new User("Admin", "User", "admin", "encoded", "admin@example.com", "123", LocalDate.now(), "Addr", Role.ROLE_ADMIN, ch.goodone.angularai.backend.model.UserStatus.ACTIVE)));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Existing\",\"lastName\":\"User\",\"login\":\"admin\",\"email\":\"admin@example.com\",\"phone\":\"123456\",\"password\":\"Password@123\",\"birthDate\":\"1990-01-01\",\"address\":\"Address\",\"role\":\"ROLE_USER\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User already exists"));
    }

    @Test
    void register_shouldReturnBadRequest_whenEmailExists() throws Exception {
        UserDTO userDTO = new UserDTO(null, "New", "User", "newuser", "admin@example.com", "123456", "PENDING", LocalDate.of(1990, 1, 1), "Address", "ROLE_USER");
        userDTO.setPassword("Password@123");

        when(userRepository.findByLogin("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(new User("Admin", "User", "admin", "encoded", "admin@example.com", "123", LocalDate.now(), "Addr", Role.ROLE_ADMIN, ch.goodone.angularai.backend.model.UserStatus.ACTIVE)));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"New\",\"lastName\":\"User\",\"login\":\"newuser\",\"email\":\"admin@example.com\",\"phone\":\"123456\",\"password\":\"Password@123\",\"birthDate\":\"1990-01-01\",\"address\":\"Address\",\"role\":\"ROLE_USER\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email already exists"));
    }

    @Test
    void register_shouldReturnBadRequest_whenEmailInvalid() throws Exception {
        UserDTO userDTO = new UserDTO(null, "New", "User", "newuser", "invalid-email", "123456", "PENDING", LocalDate.of(2000, 1, 1), "New Address", "ROLE_USER");
        userDTO.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"New\",\"lastName\":\"User\",\"login\":\"newuser\",\"email\":\"invalid-email\",\"phone\":\"123456\",\"password\":\"Password@123\",\"birthDate\":\"2000-01-01\",\"address\":\"New Address\",\"role\":\"ROLE_USER\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid email format"));
    }

    @Test
    void register_shouldReturnBadRequest_whenFirstNameMissing() throws Exception {
        UserDTO userDTO = new UserDTO(null, "", "User", "newuser", "new@example.com", "123456", "PENDING", LocalDate.of(2000, 1, 1), "New Address", "ROLE_USER");
        userDTO.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("First name is required"));
    }

    @Test
    void register_shouldReturnBadRequest_whenLastNameMissing() throws Exception {
        UserDTO userDTO = new UserDTO(null, "New", "", "newuser", "new@example.com", "123456", "PENDING", LocalDate.of(2000, 1, 1), "New Address", "ROLE_USER");
        userDTO.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Last name is required"));
    }

    @Test
    void register_shouldReturnBadRequest_whenLoginMissing() throws Exception {
        UserDTO userDTO = new UserDTO(null, "New", "User", "", "new@example.com", "123456", "PENDING", LocalDate.of(2000, 1, 1), "New Address", "ROLE_USER");
        userDTO.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Login is required"));
    }

    @Test
    void register_shouldReturnBadRequest_whenPasswordMissing() throws Exception {
        UserDTO userDTO = new UserDTO(null, "New", "User", "newuser", "new@example.com", "123456", "PENDING", LocalDate.of(2000, 1, 1), "New Address", "ROLE_USER");
        // No password set

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Password is required"));
    }

    @Test
    void register_shouldReturnBadRequest_whenEmailMissing() throws Exception {
        UserDTO userDTO = new UserDTO(null, "New", "User", "newuser", "", "123456", "PENDING", LocalDate.of(2000, 1, 1), "New Address", "ROLE_USER");
        userDTO.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"New\",\"lastName\":\"User\",\"login\":\"newuser\",\"email\":\"\",\"phone\":\"123456\",\"password\":\"Password@123\",\"birthDate\":\"2000-01-01\",\"address\":\"New Address\",\"role\":\"ROLE_USER\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email is required"));
    }

    @Test
    void register_shouldReturnBadRequest_whenPasswordWeak() throws Exception {
        UserDTO userDTO = new UserDTO(null, "New", "User", "newuser", "new@example.com", "123456", "PENDING", LocalDate.of(2000, 1, 1), "New Address", "ROLE_USER");
        userDTO.setPassword("weak");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"New\",\"lastName\":\"User\",\"login\":\"newuser\",\"email\":\"new@example.com\",\"phone\":\"123456\",\"password\":\"weak\",\"birthDate\":\"2000-01-01\",\"address\":\"New Address\",\"role\":\"ROLE_USER\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Password does not meet requirements"));
    }

    @Test
    void register_shouldCreateUser_withoutPhone() throws Exception {
        String json = "{\"firstName\":\"New\",\"lastName\":\"User\",\"login\":\"newuser\",\"email\":\"new@example.com\",\"password\":\"Password@123\",\"recaptchaToken\":\"test-token\",\"address\":\"New Address\",\"role\":\"ROLE_USER\"}";

        when(userRepository.findByLogin("newuser")).thenReturn(Optional.empty());
        when(captchaService.verify("test-token")).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("newuser"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN_READ"));

        org.mockito.ArgumentCaptor<User> userCaptor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        org.junit.jupiter.api.Assertions.assertNull(userCaptor.getValue().getPhone());
    }

    @Test
    void register_shouldCreateUser_withEmptyPhoneAsNull() throws Exception {
        String json = "{\"firstName\":\"New\",\"lastName\":\"User\",\"login\":\"newuser2\",\"email\":\"new2@example.com\",\"phone\":\" \",\"password\":\"Password@123\",\"recaptchaToken\":\"test-token\",\"address\":\"New Address\",\"role\":\"ROLE_USER\"}";

        when(userRepository.findByLogin("newuser2")).thenReturn(Optional.empty());
        when(captchaService.verify("test-token")).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        org.mockito.ArgumentCaptor<User> userCaptor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository, org.mockito.Mockito.atLeastOnce()).save(userCaptor.capture());
        
        // Find the one for newuser2 if multiple saves happened (unlikely in this isolated test, but safe)
        User capturedUser = userCaptor.getAllValues().stream()
                .filter(u -> "newuser2".equals(u.getLogin()))
                .findFirst()
                .orElseThrow();
        
        org.junit.jupiter.api.Assertions.assertNull(capturedUser.getPhone());
    }

    @Test
    void register_shouldReturnBadRequest_whenBirthDateInvalid() throws Exception {
        String json = "{\"firstName\":\"New\",\"lastName\":\"User\",\"login\":\"newuser\",\"email\":\"new@example.com\",\"phone\":\"123456\",\"password\":\"password123\",\"birthDate\":\"invalid-date\"}";

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void logout_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void register_shouldReturnBadRequest_whenUserDTOIsNull() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldFail_whenCaptchaInvalid() throws Exception {
        when(captchaService.verify(org.mockito.ArgumentMatchers.anyString())).thenReturn(false);
        when(captchaService.verify(null)).thenReturn(false);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"New\",\"lastName\":\"User\",\"login\":\"newuser\",\"email\":\"new@example.com\",\"password\":\"password123\",\"birthDate\":\"2000-01-01\",\"address\":\"New Address\",\"role\":\"ROLE_USER\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("reCAPTCHA verification failed"));
    }

    @Test
    void login_shouldReturnUnauthorized_whenUserNotFound() throws Exception {
        String login = "nonexistent";
        String password = "password";
        when(userRepository.findByLogin(login)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .with(httpBasic(login, password)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void handleJsonError_shouldReturnBadRequest_whenDateInvalid() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"New\",\"lastName\":\"User\",\"login\":\"newuser\",\"email\":\"new@example.com\",\"password\":\"password123\",\"birthDate\":\"invalid-date\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid date format. Please use yyyy-MM-dd"));
    }

    @Test
    void handleJsonError_shouldReturnBadRequest_whenJsonMalformed() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"New\", malformed}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid request data"));
    }

    @Test
    void login_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void verify_shouldReturnOk_whenTokenValid() throws Exception {
        String tokenValue = "valid-token";
        User user = new User();
        user.setLogin("testuser");
        ch.goodone.angularai.backend.model.VerificationToken token = new ch.goodone.angularai.backend.model.VerificationToken(user);
        token.setToken(tokenValue);

        when(verificationTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));

        mockMvc.perform(get("/api/auth/verify").param("token", tokenValue))
                .andExpect(status().isOk());

        verify(userRepository).save(user);
        verify(verificationTokenRepository).delete(token);
        assert user.getStatus() == ch.goodone.angularai.backend.model.UserStatus.ACTIVE;
    }

    @Test
    void verify_shouldReturnBadRequest_whenTokenExpired() throws Exception {
        String tokenValue = "expired-token";
        User user = new User();
        user.setEmail("test@example.com");
        ch.goodone.angularai.backend.model.VerificationToken token = new ch.goodone.angularai.backend.model.VerificationToken(user);
        token.setToken(tokenValue);
        token.setExpiryDate(java.time.LocalDateTime.now().minusHours(1));

        when(verificationTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));

        mockMvc.perform(get("/api/auth/verify").param("token", tokenValue))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("expired"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void verify_shouldReturnBadRequest_whenTokenInvalid() throws Exception {
        when(verificationTokenRepository.findByToken("invalid")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/auth/verify").param("token", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("invalid"));
    }

    @Test
    void resendVerification_shouldSendEmail_whenEmailExists() throws Exception {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setLogin("testuser");
        user.setStatus(ch.goodone.angularai.backend.model.UserStatus.PENDING);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/auth/resend-verification")
                        .with(csrf())
                        .param("email", email))
                .andExpect(status().isOk());

        verify(verificationTokenRepository).deleteByUser(user);
        verify(verificationTokenRepository).save(any());
        verify(emailService).sendVerificationEmail(eq(email), anyString());
    }

    @Test
    void resendVerification_shouldReturnBadRequest_whenEmailNotFound() throws Exception {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/resend-verification")
                        .with(csrf())
                        .param("email", "missing@example.com"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email not found"));
    }

    @Test
    void forgotPassword_shouldSendEmail_whenUserExists() throws Exception {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setLogin("testuser");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/auth/forgot-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email))))
                .andExpect(status().isOk());

        verify(passwordRecoveryTokenRepository).deleteByUser(user);
        verify(passwordRecoveryTokenRepository).save(any(PasswordRecoveryToken.class));
        verify(emailService).sendPasswordRecoveryEmail(eq(email), anyString(), any());
    }

    @Test
    void forgotPassword_shouldReturnOk_whenUserNotFound() throws Exception {
        String email = "missing@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/forgot-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email))))
                .andExpect(status().isOk());

        verify(passwordRecoveryTokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordRecoveryEmail(anyString(), anyString(), any());
    }

    @Test
    void resetPassword_shouldUpdatePassword_whenTokenValid() throws Exception {
        String tokenValue = "valid-reset-token";
        String newPassword = "NewPassword123!";
        User user = new User();
        user.setLogin("testuser");
        PasswordRecoveryToken token = new PasswordRecoveryToken(user);
        token.setToken(tokenValue);

        when(passwordRecoveryTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));

        mockMvc.perform(post("/api/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("token", tokenValue, "password", newPassword))))
                .andExpect(status().isOk());

        verify(userRepository).save(user);
        verify(passwordRecoveryTokenRepository).delete(token);
    }

    @Test
    void resetPassword_shouldReturnBadRequest_whenTokenExpired() throws Exception {
        String tokenValue = "expired-token";
        User user = new User();
        user.setLogin("testuser");
        PasswordRecoveryToken token = new PasswordRecoveryToken(user);
        token.setToken(tokenValue);
        token.setExpiryDate(java.time.LocalDateTime.now().minusHours(1));

        when(passwordRecoveryTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));

        mockMvc.perform(post("/api/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("token", tokenValue, "password", "NewPass123!"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("expired"));
    }

    @Test
    void resetPassword_shouldReturnBadRequest_whenTokenInvalid() throws Exception {
        when(passwordRecoveryTokenRepository.findByToken("invalid")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("token", "invalid", "password", "NewPass123!"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid"));
    }

    @Test
    void resetPassword_shouldReturnBadRequest_whenPasswordWeak() throws Exception {
        mockMvc.perform(post("/api/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("token", "some-token", "password", "weak"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Password does not meet requirements"));
    }
}
