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
    private String shortUrlBase = "";
    private Integer shortCodeLength = 8;
    private String base62Alphabet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    // URL Expiration
    private Long defaultUrlExpirationDays = 365L;

    // Cache Configuration
    private Long cacheTtlHours = 24L;

    // Rate Limiting
    private Integer rateLimitRequestsPerMinute = 100;

    // Security & JWT
    private String jwtSecret;
    private Long jwtExpirationMs = 86400000L;  // 24 hours
    private String allowedOrigins = "";
    private Integer passwordMinLength = 8;
}