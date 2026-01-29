package ch.goodone.angularai.backend.controller;

import ch.goodone.angularai.backend.dto.UserDTO;
import ch.goodone.angularai.backend.model.PasswordRecoveryToken;
import ch.goodone.angularai.backend.model.User;
import ch.goodone.angularai.backend.model.VerificationToken;
import ch.goodone.angularai.backend.repository.PasswordRecoveryTokenRepository;
import ch.goodone.angularai.backend.repository.UserRepository;
import ch.goodone.angularai.backend.repository.VerificationTokenRepository;
import ch.goodone.angularai.backend.service.ActionLogService;
import ch.goodone.angularai.backend.service.CaptchaService;
import ch.goodone.angularai.backend.service.EmailService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;

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
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordRecoveryTokenRepository passwordRecoveryTokenRepository;

    private static final String INVALID_VALUE = "invalid";
    private static final String ERROR_VALUE = "error";

    @org.springframework.beans.factory.annotation.Value("${app.base-url}")
    private String baseUrl;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, ActionLogService actionLogService, CaptchaService captchaService, EmailService emailService, VerificationTokenRepository verificationTokenRepository, PasswordRecoveryTokenRepository passwordRecoveryTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.actionLogService = actionLogService;
        this.captchaService = captchaService;
        this.emailService = emailService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordRecoveryTokenRepository = passwordRecoveryTokenRepository;
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
        
        ResponseEntity<Object> validationResponse = validateRegistration(userDTO);
        if (validationResponse != null) {
            return validationResponse;
        }

        cleanupPendingUser(userDTO.getLogin(), userDTO.getEmail());

        User user = new User();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setLogin(userDTO.getLogin());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone() != null && !userDTO.getPhone().isBlank() ? userDTO.getPhone() : null);
        user.setBirthDate(userDTO.getBirthDate());
        user.setAddress(userDTO.getAddress());
        user.setRole(ch.goodone.angularai.backend.model.Role.ROLE_ADMIN_READ);
        user.setStatus(ch.goodone.angularai.backend.model.UserStatus.PENDING);

        userRepository.save(user);
        
        VerificationToken verificationToken = new VerificationToken(user);
        verificationTokenRepository.save(verificationToken);
        
        emailService.sendVerificationEmail(user.getEmail(), verificationToken.getToken());

        actionLogService.log(user.getLogin(), "USER_REGISTERED", "User registered, pending verification. Token: " + verificationToken.getToken());
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    private ResponseEntity<Object> validateRegistration(UserDTO userDTO) {
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
        if (userDTO.getPassword().length() < 8 || !containsLetter(userDTO.getPassword()) || !containsSpecialChar(userDTO.getPassword())) {
            return ResponseEntity.badRequest().body("Password does not meet requirements");
        }
        if (userDTO.getEmail() == null || userDTO.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        if (!userDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return ResponseEntity.badRequest().body("Invalid email format");
        }
        
        return validateUserExists(userDTO.getLogin(), userDTO.getEmail());
    }

    private ResponseEntity<Object> validateUserExists(String login, String email) {
        java.util.Optional<User> byLogin = userRepository.findByLogin(login);
        if (byLogin.isPresent() && byLogin.get().getStatus() != ch.goodone.angularai.backend.model.UserStatus.PENDING) {
            return ResponseEntity.badRequest().body("User already exists");
        }
        java.util.Optional<User> byEmail = userRepository.findByEmail(email);
        if (byEmail.isPresent() && byEmail.get().getStatus() != ch.goodone.angularai.backend.model.UserStatus.PENDING) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        return null;
    }

    private void cleanupPendingUser(String login, String email) {
        // Handle re-registration: clean up pending user with same login or email
        userRepository.findByLogin(login).ifPresent(u -> {
            if (u.getStatus() == ch.goodone.angularai.backend.model.UserStatus.PENDING) {
                actionLogService.log(u.getLogin(), "USER_REREGISTER_CLEANUP", "Cleaning up pending user for re-registration by login: " + u.getLogin());
                verificationTokenRepository.deleteByUser(u);
                userRepository.delete(u);
                userRepository.flush();
            }
        });
        userRepository.findByEmail(email).ifPresent(u -> {
            if (u.getStatus() == ch.goodone.angularai.backend.model.UserStatus.PENDING) {
                actionLogService.log(u.getLogin(), "USER_REREGISTER_CLEANUP", "Cleaning up pending user for re-registration by email: " + u.getEmail());
                verificationTokenRepository.deleteByUser(u);
                userRepository.delete(u);
                userRepository.flush();
            }
        });
    }

    @GetMapping("/verify")
    public ResponseEntity<Object> verify(@RequestParam String token) {
        logger.info("Received verification request");
        return verificationTokenRepository.findByToken(token)
                .map(t -> {
                    logger.info("Token found for user");
                    if (t.isExpired()) {
                        logger.warn("Token expired for user");
                        return ResponseEntity.badRequest().<Object>body(java.util.Map.of("reason", "expired", "email", t.getUser().getEmail()));
                    }
                    User user = t.getUser();
                    user.setStatus(ch.goodone.angularai.backend.model.UserStatus.ACTIVE);
                    userRepository.save(user);
                    verificationTokenRepository.delete(t);
                    logger.info("User successfully verified and token deleted");
                    return ResponseEntity.ok().build();
                })
                .orElseGet(() -> {
                    logger.error("Token NOT found in database");
                    return ResponseEntity.badRequest().body(java.util.Map.of("reason", INVALID_VALUE));
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
                    verificationTokenRepository.deleteByUser(user);
                    
                    VerificationToken newToken = new VerificationToken(user);
                    verificationTokenRepository.save(newToken);
                    
                    emailService.sendVerificationEmail(user.getEmail(), newToken.getToken());
                    actionLogService.log(user.getLogin(), "USER_VERIFICATION_RESENT", "Verification email resent to " + email);
                    
                    return ResponseEntity.ok("Verification email sent");
                })
                .orElse(ResponseEntity.badRequest().body("Email not found"));
    }

    @PostMapping("/forgot-password")
    @Transactional
    public ResponseEntity<Void> forgotPassword(@RequestBody Map<String, String> payload, Locale locale) {
        String email = payload.get("email");
        logger.info("Password recovery requested");
        
        if (email != null) {
            userRepository.findByEmail(email).ifPresent(user -> {
                // Clean up old tokens
                passwordRecoveryTokenRepository.deleteByUser(user);
                
                PasswordRecoveryToken token = new PasswordRecoveryToken(user);
                passwordRecoveryTokenRepository.save(token);
                
                emailService.sendPasswordRecoveryEmail(user.getEmail(), token.getToken(), locale);
                actionLogService.log(user.getLogin(), "USER_PASSWORD_RECOVERY_REQUESTED", "Password recovery token generated");
            });
        }
        
        // Always return OK to prevent user enumeration
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @Transactional
    public ResponseEntity<Object> resetPassword(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        String newPassword = payload.get("password");
        
        logger.info("Password reset attempt with token");
        
        if (token == null) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_VALUE, INVALID_VALUE));
        }

        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_VALUE, "Password is required"));
        }
        if (newPassword.length() < 8 || !containsLetter(newPassword) || !containsSpecialChar(newPassword)) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_VALUE, "Password does not meet requirements"));
        }

        return passwordRecoveryTokenRepository.findByToken(token)
                .<ResponseEntity<Object>>map(t -> {
                    if (t.isExpired()) {
                        logger.warn("Password recovery token expired");
                        return ResponseEntity.badRequest().body(Map.of(ERROR_VALUE, "expired"));
                    }
                    
                    User user = t.getUser();
                    user.setPassword(passwordEncoder.encode(newPassword));
                    userRepository.save(user);
                    
                    passwordRecoveryTokenRepository.delete(t);
                    
                    actionLogService.log(user.getLogin(), "USER_PASSWORD_RESET", "Password successfully reset via recovery token");
                    logger.info("Password successfully reset for user");
                    
                    return ResponseEntity.ok().build();
                })
                .orElseGet(() -> {
                    logger.error("Invalid password recovery token");
                    return ResponseEntity.badRequest().body(Map.of(ERROR_VALUE, INVALID_VALUE));
                });
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

    private boolean containsLetter(String s) {
        for (char c : s.toCharArray()) {
            if (Character.isLetter(c)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsSpecialChar(String s) {
        for (char c : s.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                return true;
            }
        }
        return false;
    }
}
