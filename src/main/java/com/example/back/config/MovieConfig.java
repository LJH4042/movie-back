package com.example.back.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class MovieConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.create("https://api.themoviedb.org/3"); //RestClient 생성
    }
}