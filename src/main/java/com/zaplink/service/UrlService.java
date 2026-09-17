package com.zaplink.service;

import com.zaplink.config.AppProperties;
import com.zaplink.dto.ShortenRequest;
import com.zaplink.dto.ShortenResponse;
import com.zaplink.entity.Url;
import com.zaplink.entity.User;
import com.zaplink.exception.InvalidUrlException;
import com.zaplink.exception.UrlNotFoundException;
import com.zaplink.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UrlService {

    private final UrlRepository urlRepository;
    private final UrlEncodingService encodingService;
    private final AppProperties appProperties;

    public ShortenResponse shortenUrl(ShortenRequest request, User user) {
        log.info("Shortening URL: {}", request.getOriginalUrl());

        validateUrl(request.getOriginalUrl());

        String shortCode = request.getCustomCode() != null
                ? request.getCustomCode()
                : generateShortCode();

        if (urlRepository.existsByShortCode(shortCode)) {
            throw new InvalidUrlException("Short code already exists: " + shortCode);
        }

        LocalDateTime expiresAt = request.getExpiresAt();
        if (expiresAt == null && appProperties.getDefaultUrlExpirationDays() > 0) {
            expiresAt = LocalDateTime.now()
                    .plusDays(appProperties.getDefaultUrlExpirationDays());
        }

        Url url = Url.builder()
                .shortCode(shortCode)
                .originalUrl(request.getOriginalUrl())
                .user(user)  // Associate with user
                .expiresAt(expiresAt)
                .build();

        url = urlRepository.save(url);
        log.info("URL shortened: {} -> {}", request.getOriginalUrl(), shortCode);

        return mapToResponse(url);
    }

    private String generateShortCode() {
        int targetLength = appProperties.getShortCodeLength();
        long timestamp = System.nanoTime();
        long random = (long) (Math.random() * 1000);
        long combined = timestamp ^ random;

        String code = encodingService.encodeToBase62(Math.abs(combined));

        while (code.length() < targetLength) {
            code = "0" + code;
        }

        return code.substring(0, Math.min(targetLength, code.length()));
    }

    @Transactional(readOnly = true)
    public Url getUrlByShortCode(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("URL not found: " + shortCode));

        if (url.isExpired()) {
            throw new UrlNotFoundException("URL has expired: " + shortCode);
        }

        if (url.getIsDeleted()) {
            throw new UrlNotFoundException("URL has been deleted: " + shortCode);
        }

        return url;
    }

    /**
     * Delete URL (only owner can delete)
     */
    public void deleteUrl(String shortCode, User user) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("URL not found: " + shortCode));

        // Check ownership
        if (url.getUser() != null && !url.getUser().getId().equals(user.getId())) {
            throw new InvalidUrlException("You don't have permission to delete this URL");
        }

        url.setIsDeleted(true);
        urlRepository.save(url);

        log.info("URL deleted: {} by user: {}", shortCode, user.getUsername());
    }

    private void validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new InvalidUrlException("URL cannot be empty");
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new InvalidUrlException("URL must start with http:// or https://");
        }

        if (url.length() > 2048) {
            throw new InvalidUrlException("URL is too long (max 2048 characters)");
        }
    }

    private ShortenResponse mapToResponse(Url url) {
        return ShortenResponse.builder()
                .shortCode(url.getShortCode())
                .shortUrl(appProperties.getShortUrlBase() + "/v1/" + url.getShortCode())
                .originalUrl(url.getOriginalUrl())
                .createdAt(url.getCreatedAt())
                .expiresAt(url.getExpiresAt())
                .build();
    }
}