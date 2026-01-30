package ch.goodone.angularai.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@org.springframework.test.context.TestPropertySource(locations = "classpath:test-common.properties")
@org.springframework.test.context.ActiveProfiles("test")
class AibackendApplicationTests {

	@org.springframework.test.context.bean.override.mockito.MockitoBean
	private org.springframework.mail.javamail.JavaMailSender javaMailSender;

	@Test
	void contextLoads() {
	}

}
