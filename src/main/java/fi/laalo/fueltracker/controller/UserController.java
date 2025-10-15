package fi.laalo.fueltracker.controller;

import fi.laalo.fueltracker.model.User;
import fi.laalo.fueltracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // Dev use, Remove later

public class UserController {

@Autowired
private UserService userService;

// Create new user
@PostMapping
public User createUser(@RequestBody User user) {
    return userService.createUser(user);
}

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



}