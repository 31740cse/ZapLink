package com.zaplink.service;

import com.zaplink.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UrlEncodingService {

    private final AppProperties appProperties;

    /**
     * Encodes a numeric ID to Base62 string
     */
    public String encodeToBase62(long id) {
        String alphabet = appProperties.getBase62Alphabet();
        int base = alphabet.length();

        if (id == 0) {
            return String.valueOf(alphabet.charAt(0));
        }

        StringBuilder result = new StringBuilder();
        while (id > 0) {
            result.insert(0, alphabet.charAt((int) (id % base)));
            id /= base;
        }

        return result.toString();
    }

    /**
     * Decodes a Base62 string to numeric ID
     */
    public long decodeFromBase62(String code) {
        String alphabet = appProperties.getBase62Alphabet();
        int base = alphabet.length();
        long result = 0;

        for (char c : code.toCharArray()) {
            result = result * base + alphabet.indexOf(c);
        }

        return result;
    }
}