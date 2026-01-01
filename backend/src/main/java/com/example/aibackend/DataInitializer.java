package com.example.aibackend;

import com.example.aibackend.model.User;
import com.example.aibackend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                User admin = new User(
                        "Admin",
                        "User",
                        "admin",
                        passwordEncoder.encode("admin123"),
                        LocalDate.of(1990, 1, 1),
                        "123 Main St"
                );
                userRepository.save(admin);
            }
        };
    }
}
