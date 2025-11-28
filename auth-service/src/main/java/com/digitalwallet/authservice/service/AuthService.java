package com.digitalwallet.authservice.service;

import com.digitalwallet.authservice.dto.RegisterRequest;
import com.digitalwallet.authservice.dto.LoginRequest;
import com.digitalwallet.authservice.dto.AuthResponse;
import com.digitalwallet.authservice.dto.ValidateResponse;
import com.digitalwallet.authservice.entity.User;
import com.digitalwallet.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.digitalwallet.authservice.service.JwtService jwtService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);
        return "User registered successfully";
    }

    public AuthResponse login(LoginRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOptional.get().getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        User user = userOptional.get();
        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(token);
    }

    public ValidateResponse validateToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token != null && jwtService.validateToken(token)) {
            String email = jwtService.extractEmail(token);
            Optional<User> user = userRepository.findByEmail(email);

            if (user.isPresent()) {
                return new ValidateResponse(true, user.get().getId(), user.get().getEmail());
            }
        }

        return new ValidateResponse(false, null, null);
    }
}