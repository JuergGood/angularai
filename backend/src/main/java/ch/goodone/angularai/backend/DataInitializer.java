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

    @Value("${user2.password:user123}")
    private String user2Password;

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

                User admin = new User("admin", adminMail);
                admin.setFirstName("Admin");
                admin.setLastName("User");
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setPhone("+41791234567");
                admin.setBirthDate(LocalDate.of(1990, 1, 1));
                admin.setAddress("123 Main St");
                admin.setRole(ch.goodone.angularai.backend.model.Role.ROLE_ADMIN);
                admin = userRepository.save(admin);

                User user = new User("user", userMail);
                user.setFirstName("Normal");
                user.setLastName("User");
                user.setPassword(passwordEncoder.encode(userPassword));
                user.setPhone("+41797654321");
                user.setBirthDate(LocalDate.of(1995, 5, 5));
                user.setAddress("456 User Ave");
                user.setRole(ch.goodone.angularai.backend.model.Role.ROLE_USER);
                userRepository.save(user);
                
                User adminRead = new User("admin-read", adminReadMail);
                adminRead.setFirstName("Read-Only");
                adminRead.setLastName("Admin");
                adminRead.setPassword(passwordEncoder.encode(adminReadPassword));
                adminRead.setPhone("+41790000000");
                adminRead.setBirthDate(LocalDate.of(1992, 2, 2));
                adminRead.setAddress("789 Read St");
                adminRead.setRole(ch.goodone.angularai.backend.model.Role.ROLE_ADMIN_READ);
                userRepository.save(adminRead);
                
                User user2 = new User("user2", "user2@system.local");
                user2.setFirstName("Test");
                user2.setLastName("User 2");
                user2.setPassword(passwordEncoder.encode(user2Password));
                user2.setPhone("+41792222222");
                user2.setBirthDate(LocalDate.of(1998, 8, 8));
                user2.setAddress("222 Test St");
                user2.setRole(ch.goodone.angularai.backend.model.Role.ROLE_USER);
                userRepository.save(user2);

                Task task1 = new Task("Setup Project", "Initial project setup with Spring Boot", LocalDate.now().plusDays(1), Priority.HIGH, admin);
                Task task2 = new Task("Implement Login", "Create login page and auth service", LocalDate.now().plusDays(2), Priority.MEDIUM, admin);
                taskRepository.save(task1);
                taskRepository.save(task2);
                logger.info("Sample data initialized for admin, admin-read, user and user2");
            } else {
                logger.info("Database already contains data, skipping initialization");
            }
        };
    }
}
