package com.zaplink.service;

import com.zaplink.config.AppProperties;
import com.zaplink.dto.AuthResponse;
import com.zaplink.dto.LoginRequest;
import com.zaplink.dto.RegisterRequest;
import com.zaplink.dto.UserResponse;
import com.zaplink.entity.User;
import com.zaplink.exception.InvalidUrlException;
import com.zaplink.repository.UserRepository;
import com.zaplink.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AppProperties appProperties;

    /**
     * Register new user
     */
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering user: {}", request.getUsername());

        // Validate input
        if (request.getPassword().length() < appProperties.getPasswordMinLength()) {
            throw new InvalidUrlException(
                    "Password must be at least " + appProperties.getPasswordMinLength() + " characters"
            );
        }

        // Check if username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new InvalidUrlException("Username already exists: " + request.getUsername());
        }

        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new InvalidUrlException("Email already registered: " + request.getEmail());
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .build();

        user = userRepository.save(user);

        // Generate token
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());

        log.info("User registered successfully: {}", user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .message("User registered successfully")
                .build();
    }

    /**
     * Login user
     */
    public AuthResponse login(LoginRequest request) {
        log.info("User login attempt: {}", request.getUsername());

        try {
            // Authenticate
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Get user
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new InvalidUrlException("User not found"));

            // Generate token
            String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());

            log.info("User logged in successfully: {}", user.getUsername());

            return AuthResponse.builder()
                    .token(token)
                    .username(user.getUsername())
                    .message("Login successful")
                    .build();

        } catch (Exception e) {
            log.warn("Login failed for user: {}", request.getUsername());
            throw new InvalidUrlException("Invalid username or password");
        }
    }

    /**
     * Get user info
     */
    @Transactional(readOnly = true)
    public UserResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidUrlException("User not found"));

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }
}