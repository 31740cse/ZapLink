package com.zaplink.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "zaplink")
@Data
public class AppProperties {

    private String name = "ZapLink";
    private String version = "1.0.0";

    // URL Configuration
    private String shortUrlBase = "http://localhost:8080/api";
    private Integer shortCodeLength = 8;
    private String base62Alphabet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    // URL Expiration
    private Long defaultUrlExpirationDays = 365L;

    // Cache Configuration
    private Long cacheTtlHours = 24L;

    // Rate Limiting
    private Integer rateLimitRequestsPerMinute = 100;

    // Security & JWT
    private String jwtSecret = "your-super-secret-key-make-it-very-long-at-least-32-characters-long";
    private Long jwtExpirationMs = 86400000L;  // 24 hours
    private String allowedOrigins = "http://localhost:3000,http://localhost:8000,http://localhost:80";
    private Integer passwordMinLength = 8;
}