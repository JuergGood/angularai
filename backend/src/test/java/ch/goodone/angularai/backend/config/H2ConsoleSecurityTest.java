package ch.goodone.angularai.backend.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@org.springframework.test.context.TestPropertySource(locations = "classpath:test-common.properties")
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class H2ConsoleSecurityTest {

    @MockitoBean
    private org.springframework.mail.javamail.JavaMailSender javaMailSender;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void h2ConsoleShouldNotBePublic() throws Exception {
        mockMvc.perform(get("/h2-console"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void h2ConsoleShouldBeAccessibleByAdmin() throws Exception {
        mockMvc.perform(get("/h2-console/").with(user("admin").roles("ADMIN")))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401 || status == 403) {
                        throw new AssertionError("Expected status not to be 401 or 403 but was " + status);
                    }
                });
    }

    @Test
    void h2ConsoleShouldNotBeAccessibleByRegularUser() throws Exception {
        mockMvc.perform(get("/h2-console").with(user("user").roles("USER")))
                .andExpect(status().isForbidden());
    }
}
