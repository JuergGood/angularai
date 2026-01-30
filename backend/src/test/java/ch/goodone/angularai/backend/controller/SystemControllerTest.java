package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.service.SystemSettingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@org.springframework.test.context.TestPropertySource(locations = "classpath:test-common.properties")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SystemControllerTest {

    @MockitoBean
    private org.springframework.mail.javamail.JavaMailSender javaMailSender;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SystemSettingService systemSettingService;

    @Test
    void shouldReturnSystemInfo() throws Exception {
        when(systemSettingService.isLandingMessageEnabled()).thenReturn(true);
        mockMvc.perform(get("/api/system/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.backendVersion").exists())
                .andExpect(jsonPath("$.frontendVersion").exists())
                .andExpect(jsonPath("$.mode").exists())
                .andExpect(jsonPath("$.landingMessage").value("English Message"));
    }

    @Test
    void shouldReturnSystemInfoInGerman() throws Exception {
        when(systemSettingService.isLandingMessageEnabled()).thenReturn(true);
        mockMvc.perform(get("/api/system/info").header("Accept-Language", "de-CH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.landingMessage").value("German Message"));
    }

    @Test
    void shouldNotReturnLandingMessageWhenDisabled() throws Exception {
        when(systemSettingService.isLandingMessageEnabled()).thenReturn(false);
        mockMvc.perform(get("/api/system/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.landingMessage").isEmpty());
    }

    @Test
    void shouldReturnRecaptchaSiteKey() throws Exception {
        when(systemSettingService.getRecaptchaConfigIndex()).thenReturn(1);
        
        mockMvc.perform(get("/api/system/recaptcha-site-key"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/plain"))
                .andExpect(content().string(org.hamcrest.Matchers.notNullValue()));
    }
}
