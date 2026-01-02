package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.dto.UserDTO;
import ch.goodone.angularai.backend.model.Role;
import ch.goodone.angularai.backend.model.User;
import ch.goodone.angularai.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public AdminUserController(UserRepository userRepository, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) {
        if (userRepository.findAll().stream().anyMatch(u -> userDTO.getLogin().equals(u.getLogin()))) {
            return ResponseEntity.badRequest().body("Login already exists");
        }
        if (userDTO.getEmail() != null && userRepository.findAll().stream().anyMatch(u -> userDTO.getEmail().equals(u.getEmail()))) {
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
        return ResponseEntity.ok(convertToDTO(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO, Authentication authentication) {
        return userRepository.findById(id)
                .map(user -> {
                    // Unique email check
                    if (userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail())) {
                        if (userRepository.findAll().stream().anyMatch(u -> userDTO.getEmail().equals(u.getEmail()))) {
                            return ResponseEntity.badRequest().body("Email already exists");
                        }
                    }

                    // Self-protection: Prevent admin from removing their own admin privileges
                    if (user.getLogin().equals(authentication.getName())) {
                        if (userDTO.getRole() != null && !Role.ROLE_ADMIN.name().equals(userDTO.getRole())) {
                            return ResponseEntity.badRequest().body("Cannot remove your own admin role");
                        }
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
                    return ResponseEntity.ok(convertToDTO(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, Authentication authentication) {
        return userRepository.findById(id)
                .map(user -> {
                    // Self-protection: Prevent admin from deleting their own account
                    if (user.getLogin().equals(authentication.getName())) {
                        return ResponseEntity.badRequest().body("Cannot delete your own account");
                    }
                    userRepository.delete(user);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private UserDTO convertToDTO(User user) {
        return new UserDTO(user.getId(), user.getFirstName(), user.getLastName(), 
                user.getLogin(), user.getEmail(), user.getBirthDate(), user.getAddress(),
                user.getRole() != null ? user.getRole().name() : null);
    }
}
