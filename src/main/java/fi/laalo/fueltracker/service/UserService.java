package fi.laalo.fueltracker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import fi.laalo.fueltracker.repository.UserRepository;
import fi.laalo.fueltracker.model.User;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(userRepository.findByEmail(email));
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }




}
