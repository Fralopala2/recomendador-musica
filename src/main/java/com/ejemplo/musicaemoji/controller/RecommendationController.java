package com.ejemplo.musicaemoji.controller;

import com.ejemplo.musicaemoji.model.RecommendationResponse; // Importa el nuevo DTO
import com.ejemplo.musicaemoji.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Autowired
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/by-emojis")
    public ResponseEntity<RecommendationResponse> getRecommendationsByEmojis(@RequestParam String emojis) {
        Set<String> genres = recommendationService.recommendGenresByEmojis(emojis);
        List<String> recommendations = recommendationService.getSpotifyRecommendationsForGenres(genres);

        // Crea una instancia del nuevo DTO y la devuelve
        RecommendationResponse response = new RecommendationResponse(genres, recommendations);
        return ResponseEntity.ok(response);
    }
}