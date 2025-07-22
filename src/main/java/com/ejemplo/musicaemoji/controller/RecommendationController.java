package com.ejemplo.musicaemoji.controller;

import com.ejemplo.musicaemoji.model.EmojiMood;
import com.ejemplo.musicaemoji.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<String>> getRecommendationsByEmojis(@RequestParam String emojis) {
        Set<String> recommendedGenres = recommendationService.recommendGenresByEmojis(emojis);
        List<String> songs = recommendationService.getSampleSongsForGenres(recommendedGenres);
        return ResponseEntity.ok(songs);
    }

    // Nuevo endpoint para listar todos los EmojiMoods
    @GetMapping("/emojimoods")
    public ResponseEntity<List<EmojiMood>> getAllEmojiMoods() {
        List<EmojiMood> emojiMoods = recommendationService.getAllEmojiMoods();
        return ResponseEntity.ok(emojiMoods);
    }

    // Otros endpoints CRUD si los tienes (create, update, delete)
    @PostMapping("/emojimoods")
    public ResponseEntity<EmojiMood> createEmojiMood(@RequestBody EmojiMood emojiMood) {
        EmojiMood createdMood = recommendationService.createEmojiMood(emojiMood);
        return ResponseEntity.ok(createdMood);
    }

    @PutMapping("/emojimoods/{id}")
    public ResponseEntity<EmojiMood> updateEmojiMood(@PathVariable Long id, @RequestBody EmojiMood emojiMood) {
        EmojiMood updatedMood = recommendationService.updateEmojiMood(id, emojiMood);
        return ResponseEntity.ok(updatedMood);
    }

    @DeleteMapping("/emojimoods/{id}")
    public ResponseEntity<Void> deleteEmojiMood(@PathVariable Long id) {
        recommendationService.deleteEmojiMood(id);
        return ResponseEntity.noContent().build();
    }
}