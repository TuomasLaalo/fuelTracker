package fi.laalo.fueltracker.service;

import fi.laalo.fueltracker.model.User;
import fi.laalo.fueltracker.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = java.util.Optional.ofNullable(userRepository.findByEmail(email))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        Collection<GrantedAuthority> authorities = getAuthorities(user);
        
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }

    private Collection<GrantedAuthority> getAuthorities(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Default role for all users
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        
        // Add ADMIN role if user has admin role
        if (user.getRole() != null && user.getRole().equals("ADMIN")) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        
        return authorities;
    }
}
