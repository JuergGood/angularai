package ch.goodone.angularai.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SystemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnSystemInfo() throws Exception {
        mockMvc.perform(get("/api/system/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.backendVersion").exists())
                .andExpect(jsonPath("$.frontendVersion").exists())
                .andExpect(jsonPath("$.mode").exists());
    }

    @ActiveProfiles("postgres")
    @SpringBootTest
    @AutoConfigureMockMvc
    static class PostgresSystemControllerTest {
        @Autowired
        private MockMvc mockMvc;

        @Test
        void shouldReturnPostgresMode() throws Exception {
            mockMvc.perform(get("/api/system/info"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mode").value("Postgres"));
        }
    }

    @ActiveProfiles("h2")
    @SpringBootTest
    @AutoConfigureMockMvc
    static class H2SystemControllerTest {
        @Autowired
        private MockMvc mockMvc;

        @Test
        void shouldReturnH2Mode() throws Exception {
            mockMvc.perform(get("/api/system/info"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mode").value("H2"));
        }
    }
}
