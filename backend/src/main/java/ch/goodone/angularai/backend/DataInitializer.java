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
            // Ensure default users exist and are ACTIVE
            ensureUserExists(userRepository, passwordEncoder, "admin", adminEmail, adminPassword, "Admin", "User", ch.goodone.angularai.backend.model.Role.ROLE_ADMIN);
            ensureUserExists(userRepository, passwordEncoder, "user", userEmail, userPassword, "Normal", "User", ch.goodone.angularai.backend.model.Role.ROLE_USER);
            ensureUserExists(userRepository, passwordEncoder, "admin-read", adminReadEmail, adminReadPassword, "Read-Only", "Admin", ch.goodone.angularai.backend.model.Role.ROLE_ADMIN_READ);
            ensureUserExists(userRepository, passwordEncoder, "user2", "user2@goodone.ch", user2Password, "Test", "User 2", ch.goodone.angularai.backend.model.Role.ROLE_USER);

            if (taskRepository.count() == 0) {
                userRepository.findByLogin("admin").ifPresent(admin -> {
                    Task task1 = new Task("Setup Project", "Initial project setup with Spring Boot", LocalDate.now().plusDays(1), Priority.HIGH, admin);
                    Task task2 = new Task("Implement Login", "Create login page and auth service", LocalDate.now().plusDays(2), Priority.MEDIUM, admin);
                    taskRepository.save(task1);
                    taskRepository.save(task2);
                    logger.info("Sample tasks initialized");
                });
            }
        };
    }

    private void ensureUserExists(UserRepository userRepository, PasswordEncoder passwordEncoder, String login, String email, String password, String firstName, String lastName, ch.goodone.angularai.backend.model.Role role) {
        userRepository.findByLogin(login).ifPresentOrElse(
            user -> {
                if (user.getStatus() != ch.goodone.angularai.backend.model.UserStatus.ACTIVE) {
                    logger.info("User {} exists but is not ACTIVE. Setting to ACTIVE.", login);
                    user.setStatus(ch.goodone.angularai.backend.model.UserStatus.ACTIVE);
                    userRepository.save(user);
                }
            },
            () -> {
                logger.info("User {} does not exist. Creating default user.", login);
                String mail = (email != null && !email.isBlank()) ? email : login + "@goodone.ch";
                
                // Ensure email is unique if multiple users end up with same fallback
                if (userRepository.findByEmail(mail).isPresent()) {
                    mail = login + "-" + mail;
                }

                User user = new User(login, mail);
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setPassword(passwordEncoder.encode(password));
                user.setRole(role);
                user.setStatus(ch.goodone.angularai.backend.model.UserStatus.ACTIVE);
                userRepository.save(user);
            }
        );
    }
}
