package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.config.SecurityConfig;
import ch.goodone.angularai.backend.dto.ActionLogDTO;
import ch.goodone.angularai.backend.service.ActionLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@org.springframework.test.context.ActiveProfiles("test")
public class ActionLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private ActionLogService actionLogService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldGetLogs() throws Exception {
        ActionLogDTO logDTO = new ActionLogDTO(1L, LocalDateTime.now(), "admin", "LOGIN", "Admin logged in");
        when(actionLogService.getLogs(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(logDTO), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/admin/logs")
                        .param("page", "0")
                        .param("size", "10")
                        .param("type", "login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].login").value("admin"))
                .andExpect(jsonPath("$.content[0].action").value("LOGIN"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldDenyAccessToNonAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/logs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldGetLogsWithLoginFilter() throws Exception {
        mockMvc.perform(get("/api/admin/logs")
                        .param("type", "login"))
                .andExpect(status().isOk());
        
        verify(actionLogService).getLogs(any(), eq("login"), any(), any());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldClearLogs() throws Exception {
        mockMvc.perform(delete("/api/admin/logs"))
                .andExpect(status().isNoContent());

        verify(actionLogService).clearLogs();
    }

    @Test
    @WithMockUser(username = "adminread", roles = {"ADMIN_READ"})
    void adminReadShouldSeeLogs() throws Exception {
        ActionLogDTO logDTO = new ActionLogDTO(1L, LocalDateTime.now(), "admin", "LOGIN", "Admin logged in");
        when(actionLogService.getLogs(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(logDTO), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/admin/logs"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "adminread", roles = {"ADMIN_READ"})
    void adminReadShouldNotClearLogs() throws Exception {
        mockMvc.perform(delete("/api/admin/logs"))
                .andExpect(status().isForbidden());
    }
}
