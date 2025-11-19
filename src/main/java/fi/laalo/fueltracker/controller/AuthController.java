package fi.laalo.fueltracker.controller;

import fi.laalo.fueltracker.dto.UserRegisterRequestDTO;
import fi.laalo.fueltracker.model.User;
import fi.laalo.fueltracker.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public String register(@RequestBody UserRegisterRequestDTO dto) {

        if (userRepository.findByEmail(dto.email()) != null) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));

        userRepository.save(user);

        return "Registration successful";
    }
}
