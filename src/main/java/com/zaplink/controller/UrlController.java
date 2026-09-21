package com.zaplink.controller;

import com.zaplink.config.AppProperties;
import com.zaplink.dto.ShortenRequest;
import com.zaplink.dto.ShortenResponse;
import com.zaplink.entity.Url;
import com.zaplink.entity.User;
import com.zaplink.repository.UserRepository;
import com.zaplink.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class UrlController {

    private final UrlService urlService;
    private final UserRepository userRepository;
    private final AppProperties appProperties;

    /**
     * POST /api/v1/shorten
     * Create a shortened URL
     */
    @PostMapping("/shorten")
    public ResponseEntity<ShortenResponse> shortenUrl(@Valid @RequestBody ShortenRequest request) {
        log.info("Request to shorten URL");
        System.out.println("hello*********************************************");

        // Get current user if authenticated
        User user = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
            String username = auth.getName();
            user = userRepository.findByUsername(username).orElse(null);
        }
        ShortenResponse response = urlService.shortenUrl(request, user);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
        // there are many assholes in this world but none like you Mr. Ravi Prakash
    /**
     * GET /api/v1/{code}
     * Redirect to original URL
     */
    @GetMapping("/{shortCode}")
    public RedirectView redirectUrl(@PathVariable String shortCode) {
        log.info("Redirect request for code: {}", shortCode);
        Url url = urlService.getUrlByShortCode(shortCode);
        return new RedirectView(url.getOriginalUrl());
    }

    /**
     * GET /api/v1/{code}/expand
     * Get original URL without redirect
     */
    @GetMapping("/{shortCode}/expand")
    public ResponseEntity<ShortenResponse> expandUrl(@PathVariable String shortCode) {
        log.info("Expand request for code: {}", shortCode);
        Url url = urlService.getUrlByShortCode(shortCode);

        ShortenResponse response = ShortenResponse.builder()
                .shortCode(url.getShortCode())
                .originalUrl(url.getOriginalUrl())
                .createdAt(url.getCreatedAt())
                .expiresAt(url.getExpiresAt())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/user/urls
     * Get user's shortened URLs
     */
    @GetMapping("/user/urls")
    public ResponseEntity<List<ShortenResponse>> getUserUrls() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("Fetching URLs for user: {}", username);

        List<ShortenResponse> urls = user.getUrls().stream()
                .map(url -> ShortenResponse.builder()
                        .shortCode(url.getShortCode())
                        .shortUrl(urlService.getShortUrl(url.getShortCode()))
                        .originalUrl(url.getOriginalUrl())
                        .createdAt(url.getCreatedAt())
                        .expiresAt(url.getExpiresAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(urls);
    }

    /**
     * DELETE /api/v1/{code}
     * Delete a shortened URL
     */
    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Void> deleteUrl(@PathVariable String shortCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("Delete request for code: {} by user: {}", shortCode, username);

        urlService.deleteUrl(shortCode, user);

        return ResponseEntity.noContent().build();
    }
}