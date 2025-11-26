package com.example.regular_payment.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ApiConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
