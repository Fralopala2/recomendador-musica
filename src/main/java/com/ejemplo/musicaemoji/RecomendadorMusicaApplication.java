package com.ejemplo.musicaemoji;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class RecomendadorMusicaApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecomendadorMusicaApplication.class, args);
    }

    // Configuración CORS para permitir la comunicación con un frontend (ej. React, Angular)
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**") // Aplica a todos los endpoints bajo /api
                        .allowedOrigins("http://localhost:3000", "http://localhost:4200") // URL(s) de tu frontend
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}