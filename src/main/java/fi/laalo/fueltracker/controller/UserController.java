package fi.laalo.fueltracker.controller;

import fi.laalo.fueltracker.dto.UserRegisterRequestDTO;
import fi.laalo.fueltracker.dto.UserResponseDTO;
import fi.laalo.fueltracker.mapper.UserMapper;
import fi.laalo.fueltracker.model.User;
import fi.laalo.fueltracker.service.UserService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.Optional;



@RestController
@RequestMapping("/api/users")
public class UserController {

@Autowired
private UserService userService;

// Get current logged-in user
@GetMapping("/me")
public ResponseEntity<UserResponseDTO> getCurrentUser() {
    String email = org.springframework.security.core.context.SecurityContextHolder
            .getContext().getAuthentication().getName();
    User user = userService.getByEmail(email);
    UserResponseDTO response = UserMapper.toDto(user);
    return ResponseEntity.ok(response);
}

// Get user by email
@GetMapping("/email")
public Optional<UserResponseDTO> getUserByEmail(@RequestParam String email) {
    return userService.findByEmail(email)
            .map(UserMapper::toDto);
}

// Get user by ID
@GetMapping("/{id}")
public Optional<UserResponseDTO> getUserById(@PathVariable Long id) {
    return userService.getUserById(id)
            .map(UserMapper::toDto);
}

@PostMapping("/register")
public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserRegisterRequestDTO request) {
    User user = userService.registerNewUser(request.email(), request.password());
    UserResponseDTO response = UserMapper.toDto(user);
    return ResponseEntity.ok(response);
}
}