package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.dto.UserDTO;
import ch.goodone.angularai.backend.model.Role;
import ch.goodone.angularai.backend.model.User;
import ch.goodone.angularai.backend.repository.UserRepository;
import ch.goodone.angularai.backend.service.ActionLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin User Management", description = "Endpoints for administrators to manage users")
public class AdminUserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActionLogService actionLogService;

    public AdminUserController(UserRepository userRepository, PasswordEncoder passwordEncoder, ActionLogService actionLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.actionLogService = actionLogService;
    }

    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDTO::fromEntity)
                .toList();
    }

    @PostMapping
    public ResponseEntity<Object> createUser(@RequestBody UserDTO userDTO, Authentication authentication) {
        if (userDTO.getEmail() != null && !userDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return ResponseEntity.badRequest().body("Invalid email format");
        }
        if (userRepository.findByLogin(userDTO.getLogin()).isPresent()) {
            return ResponseEntity.badRequest().body("Login already exists");
        }
        // Responds if email already exists
        if (userDTO.getEmail() != null && userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        User user = new User();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setLogin(userDTO.getLogin());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword() != null ? userDTO.getPassword() : "password123"));
        user.setEmail(userDTO.getEmail());
        user.setBirthDate(userDTO.getBirthDate());
        user.setAddress(userDTO.getAddress());
        if (userDTO.getRole() != null) {
            user.setRole(Role.valueOf(userDTO.getRole()));
        } else {
            user.setRole(Role.ROLE_USER);
        }

        userRepository.save(user);
        actionLogService.log(authentication.getName(), "USER_CREATED", "Admin created user: " + user.getLogin());
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO, Authentication authentication) {
        if (userDTO.getEmail() != null && !userDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return ResponseEntity.badRequest().body("Invalid email format");
        }
        return userRepository.findById(id)
                .<ResponseEntity<Object>>map(user -> {
                    // Unique email check
                    if (userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail()) &&
                            userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
                        return ResponseEntity.badRequest().body("Email already exists");
                    }

                    // Self-protection: Prevent admin from removing their own admin privileges
                    if (user.getLogin().equals(authentication.getName()) &&
                            userDTO.getRole() != null && !Role.ROLE_ADMIN.name().equals(userDTO.getRole())) {
                        return ResponseEntity.badRequest().body("Cannot remove your own admin role");
                    }

                    user.setFirstName(userDTO.getFirstName());
                    user.setLastName(userDTO.getLastName());
                    user.setEmail(userDTO.getEmail());
                    user.setBirthDate(userDTO.getBirthDate());
                    user.setAddress(userDTO.getAddress());
                    if (userDTO.getRole() != null) {
                        user.setRole(Role.valueOf(userDTO.getRole()));
                    }

                    userRepository.save(user);
                    actionLogService.log(authentication.getName(), "USER_MODIFIED", "Admin modified user: " + user.getLogin());
                    return ResponseEntity.ok(UserDTO.fromEntity(user));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable Long id, Authentication authentication) {
        return userRepository.findById(id)
                .<ResponseEntity<Object>>map(user -> {
                    // Self-protection: Prevent admin from deleting their own account
                    if (user.getLogin().equals(authentication.getName())) {
                        return ResponseEntity.badRequest().body("Cannot delete your own account");
                    }
                    // Protection of standard users
                    if (List.of("admin", "user", "admin-read").contains(user.getLogin())) {
                        return ResponseEntity.badRequest().body("Cannot delete standard system users");
                    }
                    userRepository.delete(user);
                    actionLogService.log(authentication.getName(), "USER_DELETED", "Admin deleted user: " + user.getLogin());
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
