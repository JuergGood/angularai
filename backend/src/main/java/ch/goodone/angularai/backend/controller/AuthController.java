package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.dto.UserDTO;
import ch.goodone.angularai.backend.model.User;
import ch.goodone.angularai.backend.repository.UserRepository;
import ch.goodone.angularai.backend.service.ActionLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
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
        if (userDTO.getEmail() != null && !userDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
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
}
