package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.model.User;
import ch.goodone.angularai.backend.model.UserStatus;
import ch.goodone.angularai.backend.model.VerificationToken;
import ch.goodone.angularai.backend.repository.UserRepository;
import ch.goodone.angularai.backend.repository.VerificationTokenRepository;
import ch.goodone.angularai.backend.service.CaptchaService;
import ch.goodone.angularai.backend.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@org.springframework.test.context.TestPropertySource(locations = "classpath:test-common.properties")
@ActiveProfiles("test")
class VerificationIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CaptchaService captchaService;

    @MockitoBean
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        tokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testFullRegistrationAndVerificationFlow() throws Exception {
        // 1. Register
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("firstName", "Verify");
        userMap.put("lastName", "Test");
        userMap.put("login", "verifytest");
        userMap.put("email", "verify@example.com");
        userMap.put("password", "Password123!");
        userMap.put("recaptchaToken", "dummy");

        when(captchaService.verify(anyString())).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userMap)))
                .andExpect(status().isOk());

        // 2. Find token in DB
        User user1 = userRepository.findByLogin("verifytest").orElseThrow();
        assertEquals(UserStatus.PENDING, user1.getStatus());
        
        VerificationToken token = tokenRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(user1.getId()))
                .findFirst()
                .orElseThrow();
        
        String tokenValue = token.getToken();
        assertNotNull(tokenValue);

        // 3. Verify
        mockMvc.perform(get("/api/auth/verify").param("token", tokenValue))
                .andExpect(status().isOk());

        // 4. Check user is now active
        User verifiedUser = userRepository.findByLogin("verifytest").orElseThrow();
        assertEquals(UserStatus.ACTIVE, verifiedUser.getStatus());
        
        // 5. Check token is deleted
        assertTrue(tokenRepository.findByToken(tokenValue).isEmpty());
    }

    @Test
    void testVerificationWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/auth/verify").param("token", UUID.randomUUID().toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("invalid"));
    }
}
