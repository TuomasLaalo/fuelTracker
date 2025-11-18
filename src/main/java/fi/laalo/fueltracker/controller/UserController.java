package fi.laalo.fueltracker.controller;

import fi.laalo.fueltracker.model.User;
import fi.laalo.fueltracker.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Optional;



@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // Dev use, Remove later

public class UserController {

@Autowired
private UserService userService;

// Get user by email
@GetMapping("/email")
public Optional<User> getUserByEmail(@RequestParam String email) {
    return userService.findByEmail(email);
}

// Get user by ID
@GetMapping("/{id}")
public Optional<User> getUserById(@PathVariable Long id) {
    return userService.getUserById(id);
}

@PostMapping("/register")
public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
    try {
        String email = request.get("email");
        String password = request.get("password");

        userService.registerNewUser(email, password);

        return ResponseEntity.ok("User registered successfully");

    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
}