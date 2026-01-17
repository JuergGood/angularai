package ch.goodone.angularai.backend;

import org.junit.jupiter.api.Test;

class AibackendApplicationTest {

    @Test
    void main() {
        // Just calling the main method with a dummy argument to satisfy coverage.
        // We use a property to prevent the application from actually starting fully if possible,
        // or we just accept that it will start and stop.
        // Actually, SpringBootTest already covers the context loading.
        // To cover the static main method specifically:
        AibackendApplication.main(new String[]{"--server.port=0", "--spring.main.banner-mode=off"});
    }
}
