package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.config.SecurityConfig;
import ch.goodone.angularai.backend.service.ActionLogService;
import ch.goodone.angularai.backend.service.SystemSettingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@org.springframework.test.context.ActiveProfiles("test")
class AdminSystemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private SystemSettingService systemSettingService;

    @MockitoBean
    private ActionLogService actionLogService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void shouldGetGeolocationEnabled() throws Exception {
        when(systemSettingService.isGeolocationEnabled()).thenReturn(true);

        mockMvc.perform(get("/api/admin/settings/geolocation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void shouldSetGeolocationEnabled() throws Exception {
        mockMvc.perform(post("/api/admin/settings/geolocation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("enabled", true))))
                .andExpect(status().isOk());

        verify(systemSettingService).setGeolocationEnabled(true);
        verify(actionLogService).log(eq("admin"), eq("SETTING_CHANGED"), anyString());
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    void shouldDenyAccessToNonAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/settings/geolocation"))
                .andExpect(status().isForbidden());
    }
}
