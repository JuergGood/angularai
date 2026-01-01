package com.example.aibackend.controller;

import com.example.aibackend.dto.UserDTO;
import com.example.aibackend.model.User;
import com.example.aibackend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        User user = userRepository.findByLogin(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(convertToDTO(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateCurrentUser(Authentication authentication, @RequestBody UserDTO userDTO) {
        User user = userRepository.findByLogin(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setBirthDate(userDTO.getBirthDate());
        user.setAddress(userDTO.getAddress());
        
        userRepository.save(user);
        return ResponseEntity.ok(convertToDTO(user));
    }

    private UserDTO convertToDTO(User user) {
        return new UserDTO(user.getId(), user.getFirstName(), user.getLastName(), 
                user.getLogin(), user.getBirthDate(), user.getAddress());
    }
}
