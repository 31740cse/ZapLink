package com.zaplink.controller;

import com.zaplink.dto.AuthResponse;
import com.zaplink.dto.LoginRequest;
import com.zaplink.dto.RegisterRequest;
import com.zaplink.dto.UserResponse;
import com.zaplink.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/v1/auth/register
     * Register new user
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request for: {}", request.getUsername());
        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * POST /api/v1/auth/login
     * User login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for: {}", request.getUsername());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/auth/me
     * Get current user info
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Extract user ID from principal
        String username = authentication.getName();
        log.info("Fetching info for user: {}", username);

        // Since we don't have user ID directly, we'll need to fetch from database
        // This is a limitation of the current approach
        return ResponseEntity.ok(UserResponse.builder()
                .username(username)
                .build());
    }
}