package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.dto.UserDTO;
import ch.goodone.angularai.backend.model.User;
import ch.goodone.angularai.backend.repository.UserRepository;
import ch.goodone.angularai.backend.service.ActionLogService;
import ch.goodone.angularai.backend.service.CaptchaService;
import ch.goodone.angularai.backend.service.EmailService;
import ch.goodone.angularai.backend.model.VerificationToken;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user login, logout, and registration")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActionLogService actionLogService;
    private final CaptchaService captchaService;
    private final EmailService emailService;
    private final ch.goodone.angularai.backend.repository.VerificationTokenRepository tokenRepository;

    @org.springframework.beans.factory.annotation.Value("${app.base-url}")
    private String baseUrl;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, ActionLogService actionLogService, CaptchaService captchaService, EmailService emailService, ch.goodone.angularai.backend.repository.VerificationTokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.actionLogService = actionLogService;
        this.captchaService = captchaService;
        this.emailService = emailService;
        this.tokenRepository = tokenRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(Authentication authentication, HttpServletRequest request) {
        if (authentication == null) {
            logger.error("Login attempt failed: Authentication object is null");
            return ResponseEntity.status(401).build();
        }
        User user = userRepository.findByLogin(authentication.getName())
                .orElseGet(() -> {
                    logger.error("Login attempt failed: User not found for login: {}", authentication.getName());
                    return null;
                });
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        if (user.getStatus() != ch.goodone.angularai.backend.model.UserStatus.ACTIVE) {
            logger.warn("Login attempt for non-active user: {}", user.getLogin());
            return ResponseEntity.status(403).body(null);
        }
        
        String ip = request.getHeader("X-Forwarded-For");
        logger.info("Login request from IP (X-Forwarded-For): {}", ip);
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            logger.info("Login request from IP (RemoteAddr): {}", ip);
        } else if (ip.contains(",")) {
            // If there are multiple IPs, the first one is the client IP
            ip = ip.split(",")[0].trim();
            logger.info("Parsed client IP from X-Forwarded-For: {}", ip);
        }
        String ua = request.getHeader("User-Agent");
        logger.info("Login request User-Agent: {}", ua);
        
        actionLogService.logLogin(user.getLogin(), ip, ua);
        
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    @GetMapping("/info")
    public ResponseEntity<UserDTO> getAuthInfo(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return userRepository.findByLogin(authentication.getName())
                .map(user -> ResponseEntity.ok(UserDTO.fromEntity(user)))
                .orElse(ResponseEntity.status(401).build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        if (authentication != null) {
            actionLogService.log(authentication.getName(), "USER_LOGOUT", "User logged out");
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody UserDTO userDTO) {
        if (userDTO == null) {
            return ResponseEntity.badRequest().body("Invalid request data");
        }
        if (!captchaService.verify(userDTO.getRecaptchaToken())) {
            return ResponseEntity.badRequest().body("reCAPTCHA verification failed");
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
        if (userDTO.getPassword().length() < 8 || !userDTO.getPassword().matches(".*[A-Za-z].*") || !userDTO.getPassword().matches(".*[^A-Za-z0-9].*")) {
            return ResponseEntity.badRequest().body("Password does not meet requirements");
        }
        if (userDTO.getEmail() == null || userDTO.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        if (!userDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return ResponseEntity.badRequest().body("Invalid email format");
        }
        if (userRepository.findByLogin(userDTO.getLogin()).isPresent()) {
            User existingUser = userRepository.findByLogin(userDTO.getLogin()).get();
            if (existingUser.getStatus() != ch.goodone.angularai.backend.model.UserStatus.PENDING) {
                return ResponseEntity.badRequest().body("User already exists");
            }
        }
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            User existingUser = userRepository.findByEmail(userDTO.getEmail()).get();
            if (existingUser.getStatus() != ch.goodone.angularai.backend.model.UserStatus.PENDING) {
                return ResponseEntity.badRequest().body("Email already exists");
            }
        }

        // Handle re-registration: clean up pending user with same login or email
        userRepository.findByLogin(userDTO.getLogin()).ifPresent(u -> {
            if (u.getStatus() == ch.goodone.angularai.backend.model.UserStatus.PENDING) {
                actionLogService.log(u.getLogin(), "USER_REREGISTER_CLEANUP", "Cleaning up pending user for re-registration by login: " + u.getLogin());
                tokenRepository.deleteByUser(u);
                userRepository.delete(u);
                userRepository.flush();
            }
        });
        userRepository.findByEmail(userDTO.getEmail()).ifPresent(u -> {
            if (u.getStatus() == ch.goodone.angularai.backend.model.UserStatus.PENDING) {
                actionLogService.log(u.getLogin(), "USER_REREGISTER_CLEANUP", "Cleaning up pending user for re-registration by email: " + u.getEmail());
                tokenRepository.deleteByUser(u);
                userRepository.delete(u);
                userRepository.flush();
            }
        });

        User user = new User(
                userDTO.getFirstName(),
                userDTO.getLastName(),
                userDTO.getLogin(),
                passwordEncoder.encode(userDTO.getPassword()),
                userDTO.getEmail(),
                userDTO.getPhone() != null && !userDTO.getPhone().isBlank() ? userDTO.getPhone() : null,
                userDTO.getBirthDate(),
                userDTO.getAddress(),
                ch.goodone.angularai.backend.model.Role.ROLE_ADMIN_READ,
                ch.goodone.angularai.backend.model.UserStatus.PENDING
        );

        userRepository.save(user);
        
        VerificationToken token = new VerificationToken(user);
        tokenRepository.save(token);
        
        emailService.sendVerificationEmail(user.getEmail(), token.getToken());

        actionLogService.log(user.getLogin(), "USER_REGISTERED", "User registered, pending verification. Token: " + token.getToken());
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    @GetMapping("/verify")
    public ResponseEntity<Object> verify(@RequestParam String token) {
        logger.info("Received verification request for token: {}", token);
        return tokenRepository.findByToken(token)
                .map(t -> {
                    logger.info("Token found for user: {}", t.getUser().getLogin());
                    if (t.isExpired()) {
                        logger.warn("Token expired for user: {}", t.getUser().getLogin());
                        return ResponseEntity.badRequest().<Object>body(java.util.Map.of("reason", "expired", "email", t.getUser().getEmail()));
                    }
                    User user = t.getUser();
                    user.setStatus(ch.goodone.angularai.backend.model.UserStatus.ACTIVE);
                    userRepository.save(user);
                    tokenRepository.delete(t);
                    logger.info("User {} successfully verified and token deleted", user.getLogin());
                    return ResponseEntity.ok().build();
                })
                .orElseGet(() -> {
                    logger.error("Token NOT found in database: {}", token);
                    return ResponseEntity.badRequest().body(java.util.Map.of("reason", "invalid"));
                });
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerification(@RequestParam String email) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    if (user.getStatus() != ch.goodone.angularai.backend.model.UserStatus.PENDING) {
                        return ResponseEntity.badRequest().body("User is already active or invalid status");
                    }
                    // Clean up old tokens
                    tokenRepository.deleteByUser(user);
                    
                    VerificationToken newToken = new VerificationToken(user);
                    tokenRepository.save(newToken);
                    
                    emailService.sendVerificationEmail(user.getEmail(), newToken.getToken());
                    actionLogService.log(user.getLogin(), "USER_VERIFICATION_RESENT", "Verification email resent to " + email);
                    
                    return ResponseEntity.ok("Verification email sent");
                })
                .orElse(ResponseEntity.badRequest().body("Email not found"));
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleJsonError(org.springframework.http.converter.HttpMessageNotReadableException e) {
        String msg = "Invalid request data";
        String exceptionMessage = e.getMessage();
        if (exceptionMessage != null && exceptionMessage.contains("java.time.LocalDate")) {
            msg = "Invalid date format. Please use yyyy-MM-dd";
        }
        return ResponseEntity.badRequest().body(msg);
    }
}
