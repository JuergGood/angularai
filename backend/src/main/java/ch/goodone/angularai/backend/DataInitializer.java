package ch.goodone.angularai.backend;

import ch.goodone.angularai.backend.model.Priority;
import ch.goodone.angularai.backend.model.Task;
import ch.goodone.angularai.backend.model.User;
import ch.goodone.angularai.backend.repository.TaskRepository;
import ch.goodone.angularai.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    @org.springframework.context.annotation.Profile("!test")
    CommandLineRunner initData(UserRepository userRepository, TaskRepository taskRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                User admin = new User(
                        "Admin",
                        "User",
                        "admin",
                        passwordEncoder.encode("admin123"),
                        "admin@example.com",
                        LocalDate.of(1990, 1, 1),
                        "123 Main St",
                        ch.goodone.angularai.backend.model.Role.ROLE_ADMIN
                );
                admin = userRepository.save(admin);

                User user = new User(
                        "Normal",
                        "User",
                        "user",
                        passwordEncoder.encode("user123"),
                        "user@example.com",
                        LocalDate.of(1995, 5, 5),
                        "456 User Ave",
                        ch.goodone.angularai.backend.model.Role.ROLE_USER
                );
                userRepository.save(user);

                Task task1 = new Task("Setup Project", "Initial project setup with Spring Boot", LocalDate.now().plusDays(1), Priority.HIGH, admin);
                Task task2 = new Task("Implement Login", "Create login page and auth service", LocalDate.now().plusDays(2), Priority.MEDIUM, admin);
                taskRepository.save(task1);
                taskRepository.save(task2);
            }
        };
    }
}
