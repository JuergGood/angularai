package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.dto.UserDTO;
import ch.goodone.angularai.backend.model.User;
import ch.goodone.angularai.backend.model.VerificationToken;
import ch.goodone.angularai.backend.repository.UserRepository;
import ch.goodone.angularai.backend.repository.VerificationTokenRepository;
import ch.goodone.angularai.backend.service.ActionLogService;
import ch.goodone.angularai.backend.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Profile", description = "Endpoints for managing the logged-in user's profile")
public class UserController {
    
    private static final String USER_NOT_FOUND = "User not found";

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    private final ActionLogService actionLogService;

    public UserController(UserRepository userRepository, VerificationTokenRepository verificationTokenRepository, EmailService emailService, ActionLogService actionLogService) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.emailService = emailService;
        this.actionLogService = actionLogService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        User user = userRepository.findByLogin(authentication.getName())
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    @PutMapping("/me")
    @Transactional
    public ResponseEntity<Object> updateCurrentUser(Authentication authentication, @RequestBody UserDTO userDTO) {
        User user = userRepository.findByLogin(authentication.getName())
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        
        if (userDTO.getEmail() != null && !userDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return ResponseEntity.badRequest().body("Invalid email format");
        }

        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setBirthDate(userDTO.getBirthDate());
        user.setAddress(userDTO.getAddress());
        
        boolean emailChanged = false;
        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail())) {
            // Email change requires verification
            if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body("Email already exists");
            }
            
            user.setPendingEmail(userDTO.getEmail());
            
            // Clean up old tokens
            verificationTokenRepository.deleteByUser(user);
            
            VerificationToken token = new VerificationToken(user);
            verificationTokenRepository.save(token);
            
            emailService.sendVerificationEmail(user.getPendingEmail(), token.getToken());
            actionLogService.log(user.getLogin(), "USER_EMAIL_CHANGE_REQUESTED", "User requested email change to: " + user.getPendingEmail());
            emailChanged = true;
        }

        userRepository.save(user);
        actionLogService.log(user.getLogin(), "USER_MODIFIED", "User updated own profile");
        
        if (emailChanged) {
            return ResponseEntity.ok(Map.of("message", "Verification email sent to new address", "user", UserDTO.fromEntity(user)));
        }
        
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
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

        userRepository.delete(user);
        actionLogService.log(login, "USER_DELETED", "User deleted own account");
        return ResponseEntity.noContent().build();
    }
}
