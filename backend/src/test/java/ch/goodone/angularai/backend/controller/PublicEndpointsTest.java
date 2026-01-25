package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.dto.UserDTO;
import ch.goodone.angularai.backend.service.CaptchaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
public class PublicEndpointsTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CaptchaService captchaService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void recaptchaSiteKeyShouldBePublic() throws Exception {
        mockMvc.perform(get("/api/system/recaptcha-site-key"))
                .andExpect(status().isOk());
    }

    @Test
    void registerShouldBePublic() throws Exception {
        // Use a map to ensure password is included in JSON since UserDTO might have Access.WRITE_ONLY
        java.util.Map<String, Object> userMap = new java.util.HashMap<>();
        userMap.put("firstName", "Test");
        userMap.put("lastName", "User");
        userMap.put("login", "testpublic");
        userMap.put("email", "testpublic@example.com");
        userMap.put("password", "Password123!");
        userMap.put("recaptchaToken", "dummy-token");

        when(captchaService.verify(anyString())).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userMap)))
                .andExpect(status().isOk());
    }
}
