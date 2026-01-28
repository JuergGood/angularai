package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.dto.UserDTO;
import ch.goodone.angularai.backend.model.User;
import ch.goodone.angularai.backend.repository.UserRepository;
import ch.goodone.angularai.backend.service.ActionLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Profile", description = "Endpoints for managing the logged-in user's profile")
public class UserController {

    private final UserRepository userRepository;
    private final ActionLogService actionLogService;

    public UserController(UserRepository userRepository, ActionLogService actionLogService) {
        this.userRepository = userRepository;
        this.actionLogService = actionLogService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        User user = userRepository.findByLogin(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    @PutMapping("/me")
    public ResponseEntity<Object> updateCurrentUser(Authentication authentication, @RequestBody UserDTO userDTO) {
        User user = userRepository.findByLogin(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (userDTO.getEmail() != null && !userDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return ResponseEntity.badRequest().body("Invalid email format");
        }

        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setBirthDate(userDTO.getBirthDate());
        user.setAddress(userDTO.getAddress());
        
        userRepository.save(user);
        actionLogService.log(user.getLogin(), "USER_MODIFIED", "User updated own profile");
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete the logged-in user's account")
    public ResponseEntity<Object> deleteCurrentUser(Authentication authentication) {
        String login = authentication.getName();
        if (Set.of("admin", "admin-read", "user").contains(login)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("This user cannot be deleted.");
        }

        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.delete(user);
        actionLogService.log(login, "USER_DELETED", "User deleted own account");
        return ResponseEntity.noContent().build();
    }
}
