package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.config.SecurityConfig;
import ch.goodone.angularai.backend.service.ActionLogService;
import ch.goodone.angularai.backend.service.IpLocationService;
import ch.goodone.angularai.backend.service.SystemSettingService;
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

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class AdminSystemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private SystemSettingService systemSettingService;

    @MockitoBean
    private ActionLogService actionLogService;

    @MockitoBean
    private IpLocationService ipLocationService;

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
        
        verify(systemSettingService).isGeolocationEnabled();
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void shouldSetGeolocationEnabled() throws Exception {
        mockMvc.perform(post("/api/admin/settings/geolocation")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("enabled", true))))
                .andExpect(status().isOk());

        verify(systemSettingService).setGeolocationEnabled(true);
        verify(actionLogService).log(eq("admin"), eq("SETTING_CHANGED"), contains("Geolocation enabled set to: true"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void shouldTestGeolocation() throws Exception {
        IpLocationService.GeoLocation loc = new IpLocationService.GeoLocation();
        loc.setCity("Zurich");
        loc.setCountry("Switzerland");
        when(ipLocationService.lookup("8.8.8.8")).thenReturn(loc);

        mockMvc.perform(get("/api/admin/settings/geolocation/test").param("ip", "8.8.8.8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Zurich"))
                .andExpect(jsonPath("$.country").value("Switzerland"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void shouldGetRecaptchaConfigIndex() throws Exception {
        when(systemSettingService.getRecaptchaConfigIndex()).thenReturn(2);

        mockMvc.perform(get("/api/admin/settings/recaptcha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.index").value(2));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void shouldSetRecaptchaConfigIndex() throws Exception {
        mockMvc.perform(post("/api/admin/settings/recaptcha")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("index", 3))))
                .andExpect(status().isOk());

        verify(systemSettingService).setRecaptchaConfigIndex(3);
        verify(actionLogService).log(eq("admin"), eq("SETTING_CHANGED"), contains("reCAPTCHA config index set to: 3"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void shouldGetLandingMessageEnabled() throws Exception {
        when(systemSettingService.isLandingMessageEnabled()).thenReturn(true);

        mockMvc.perform(get("/api/admin/settings/landing-message"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void shouldGetLandingMessageDisabled() throws Exception {
        when(systemSettingService.isLandingMessageEnabled()).thenReturn(false);

        mockMvc.perform(get("/api/admin/settings/landing-message"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void shouldSetLandingMessageEnabled() throws Exception {
        mockMvc.perform(post("/api/admin/settings/landing-message")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("enabled", false))))
                .andExpect(status().isOk());

        verify(systemSettingService).setLandingMessageEnabled(false);
        verify(actionLogService).log(eq("admin"), eq("SETTING_CHANGED"), contains("Landing message enabled set to: false"));
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    void shouldDenyAccessToNonAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/settings/geolocation"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "adminread", authorities = {"ROLE_ADMIN_READ"})
    void adminReadShouldSeeGeolocationEnabled() throws Exception {
        when(systemSettingService.isGeolocationEnabled()).thenReturn(true);

        mockMvc.perform(get("/api/admin/settings/geolocation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
        
        verify(systemSettingService, times(1)).isGeolocationEnabled();
    }

    @Test
    @WithMockUser(username = "adminread", authorities = {"ROLE_ADMIN_READ"})
    void adminReadShouldSeeLandingMessageEnabled() throws Exception {
        when(systemSettingService.isLandingMessageEnabled()).thenReturn(true);

        mockMvc.perform(get("/api/admin/settings/landing-message"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
        
        verify(systemSettingService, times(1)).isLandingMessageEnabled();
    }

    @Test
    @WithMockUser(username = "adminread", authorities = {"ROLE_ADMIN_READ"})
    void adminReadShouldNotModifySettings() throws Exception {
        mockMvc.perform(post("/api/admin/settings/geolocation")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("enabled", true))))
                .andExpect(status().isForbidden());
    }
}
