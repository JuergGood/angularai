package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.dto.UserDTO;
import ch.goodone.angularai.backend.model.User;
import ch.goodone.angularai.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        User user = userRepository.findByLogin(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDTO userDTO) {
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
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }
}
