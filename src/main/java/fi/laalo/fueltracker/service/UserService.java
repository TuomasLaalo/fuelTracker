package fi.laalo.fueltracker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import fi.laalo.fueltracker.repository.UserRepository;
import fi.laalo.fueltracker.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User registerNewUser(String email, String rawPassword) {

    // Tarkistus: onko email jo käytössä?
    if (userRepository.findByEmail(email) != null) {
        throw new IllegalArgumentException("Email is already registered");
    }

    User user = new User();
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(rawPassword));

    return userRepository.save(user);
}

    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(userRepository.findByEmail(email));
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }




}
