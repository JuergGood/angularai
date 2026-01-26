package ch.goodone.angularai.backend;

import ch.goodone.angularai.backend.model.Priority;
import ch.goodone.angularai.backend.model.Task;
import ch.goodone.angularai.backend.model.User;
import ch.goodone.angularai.backend.repository.TaskRepository;
import ch.goodone.angularai.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${user.password}")
    private String userPassword;

    @Value("${admin.read.password}")
    private String adminReadPassword;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${user.email}")
    private String userEmail;

    @Value("${admin.read.email}")
    private String adminReadEmail;

    @Bean
    @org.springframework.context.annotation.Profile("!test")
    CommandLineRunner initData(UserRepository userRepository, TaskRepository taskRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                // Ensure unique emails even if configuration is identical (fallback)
                String adminMail = (adminEmail != null && !adminEmail.isBlank()) ? adminEmail : "admin@system.local";
                String userMail = (userEmail != null && !userEmail.isBlank()) ? userEmail : "user@system.local";
                String adminReadMail = (adminReadEmail != null && !adminReadEmail.isBlank()) ? adminReadEmail : "admin-read@system.local";

                // Secondary safety check: ensure they are not identical if coming from empty environment variables
                if (userMail.equals(adminMail)) userMail = "user-" + userMail;
                if (adminReadMail.equals(adminMail) || adminReadMail.equals(userMail)) adminReadMail = "read-" + adminReadMail;

                User admin = new User(
                        "Admin",
                        "User",
                        "admin",
                        passwordEncoder.encode(adminPassword),
                        adminMail,
                        "+41791234567",
                        LocalDate.of(1990, 1, 1),
                        "123 Main St",
                        ch.goodone.angularai.backend.model.Role.ROLE_ADMIN,
                        ch.goodone.angularai.backend.model.UserStatus.ACTIVE
                );
                admin = userRepository.save(admin);

                User user = new User(
                        "Normal",
                        "User",
                        "user",
                        passwordEncoder.encode(userPassword),
                        userMail,
                        "+41797654321",
                        LocalDate.of(1995, 5, 5),
                        "456 User Ave",
                        ch.goodone.angularai.backend.model.Role.ROLE_USER,
                        ch.goodone.angularai.backend.model.UserStatus.ACTIVE
                );
                userRepository.save(user);
                
                User adminRead = new User(
                        "Read-Only",
                        "Admin",
                        "admin-read",
                        passwordEncoder.encode(adminReadPassword),
                        adminReadMail,
                        "+41790000000",
                        LocalDate.of(1992, 2, 2),
                        "789 Read St",
                        ch.goodone.angularai.backend.model.Role.ROLE_ADMIN_READ,
                        ch.goodone.angularai.backend.model.UserStatus.ACTIVE
                );
                userRepository.save(adminRead);

                Task task1 = new Task("Setup Project", "Initial project setup with Spring Boot", LocalDate.now().plusDays(1), Priority.HIGH, admin);
                Task task2 = new Task("Implement Login", "Create login page and auth service", LocalDate.now().plusDays(2), Priority.MEDIUM, admin);
                taskRepository.save(task1);
                taskRepository.save(task2);
                logger.info("Sample data initialized for admin, admin-read and user");
            } else {
                logger.info("Database already contains data, skipping initialization");
            }
        };
    }
}
