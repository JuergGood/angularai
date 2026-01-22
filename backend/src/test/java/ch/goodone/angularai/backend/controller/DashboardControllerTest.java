package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.dto.DashboardDTO;
import ch.goodone.angularai.backend.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import ch.goodone.angularai.backend.config.SecurityConfig;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.junit.jupiter.api.BeforeEach;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void getDashboardData_ShouldReturnData() throws Exception {
        DashboardDTO dto = new DashboardDTO(
                new DashboardDTO.SummaryStats(10, 2, 5, 1, 20, 5, 100, 10),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                new DashboardDTO.TaskStatusDistribution(5, 5, 20, 0, 30)
        );

        when(dashboardService.getDashboardData()).thenReturn(dto);

        mockMvc.perform(get("/api/dashboard")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.openTasks").value(10))
                .andExpect(jsonPath("$.taskDistribution.total").value(30));
    }

    @Test
    void getDashboardData_ShouldReturnUnauthorized_WhenNoUser() throws Exception {
        mockMvc.perform(get("/api/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
