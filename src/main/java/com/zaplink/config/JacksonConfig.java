package com.zaplink.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class JacksonConfig {

    /**
     * Custom ObjectMapper configuration for Spring's JSON handling
     */
    @Bean
    public ObjectMapper customObjectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper mapper = builder
                .featuresToDisable(
                        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                        SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
                )
                .featuresToEnable(
                        DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT
                )
                .modules(new JavaTimeModule())
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .build();

        log.info("Custom ObjectMapper configured");
        return mapper;
    }
}