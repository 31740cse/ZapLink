package com.zaplink.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortenRequest {

    @NotBlank(message = "URL cannot be blank")
    private String originalUrl;

    private String customCode;

    private LocalDateTime expiresAt;
}