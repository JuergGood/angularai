package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.dto.UserDTO;
import ch.goodone.angularai.backend.model.User;
import ch.goodone.angularai.backend.repository.UserRepository;
import ch.goodone.angularai.backend.service.ActionLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user login, logout, and registration")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActionLogService actionLogService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, ActionLogService actionLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.actionLogService = actionLogService;
    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        User user = userRepository.findByLogin(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        actionLogService.log(user.getLogin(), "USER_LOGIN", "User logged in successfully");
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        if (authentication != null) {
            actionLogService.log(authentication.getName(), "USER_LOGOUT", "User logged out");
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDTO userDTO) {
        if (userDTO == null) {
            return ResponseEntity.badRequest().body("Invalid request data");
        }
        if (userDTO.getFirstName() == null || userDTO.getFirstName().isBlank()) {
            return ResponseEntity.badRequest().body("First name is required");
        }
        if (userDTO.getLastName() == null || userDTO.getLastName().isBlank()) {
            return ResponseEntity.badRequest().body("Last name is required");
        }
        if (userDTO.getLogin() == null || userDTO.getLogin().isBlank()) {
            return ResponseEntity.badRequest().body("Login is required");
        }
        if (userDTO.getPassword() == null || userDTO.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Password is required");
        }
        if (userDTO.getEmail() == null || userDTO.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        if (userDTO.getBirthDate() == null) {
            return ResponseEntity.badRequest().body("Birth date is required or invalid format. Please use yyyy-MM-dd");
        }
        if (!userDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return ResponseEntity.badRequest().body("Invalid email format");
        }
        if (userRepository.findByLogin(userDTO.getLogin()).isPresent()) {
            return ResponseEntity.badRequest().body("User already exists");
        }

        User user = new User(
                userDTO.getFirstName(),
                userDTO.getLastName(),
                userDTO.getLogin(),
                passwordEncoder.encode(userDTO.getPassword()),
                userDTO.getEmail(),
                userDTO.getBirthDate(),
                userDTO.getAddress(),
                ch.goodone.angularai.backend.model.Role.ROLE_USER
        );

        userRepository.save(user);
        actionLogService.log(user.getLogin(), "USER_REGISTERED", "User registered");
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleJsonError(org.springframework.http.converter.HttpMessageNotReadableException e) {
        String msg = "Invalid request data";
        if (e.getCause() instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException) {
            com.fasterxml.jackson.databind.exc.InvalidFormatException ife = (com.fasterxml.jackson.databind.exc.InvalidFormatException) e.getCause();
            if (ife.getTargetType().equals(LocalDate.class)) {
                msg = "Invalid date format. Please use yyyy-MM-dd";
            }
        }
        return ResponseEntity.badRequest().body(msg);
    }
}
