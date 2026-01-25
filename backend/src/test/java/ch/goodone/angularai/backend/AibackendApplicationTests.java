package ch.goodone.angularai.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"IPSTACK_API_KEY=test_key", "spring.profiles.active=test"})
class AibackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
