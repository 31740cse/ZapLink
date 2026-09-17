package com.zaplink.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
public class AppConfig {

    /**
     * RestTemplate Bean for making HTTP calls
     * Useful for validating URLs or calling external services
     */
    @Bean
    public RestTemplate restTemplate(ObjectMapper customObjectMapper) {
        RestTemplate restTemplate = new RestTemplate();

        // Configure message converters
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        MappingJackson2HttpMessageConverter converter =
                new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(customObjectMapper);
        converters.add(converter);

        restTemplate.setMessageConverters(converters);

        log.info("RestTemplate configured");
        return restTemplate;
    }
}