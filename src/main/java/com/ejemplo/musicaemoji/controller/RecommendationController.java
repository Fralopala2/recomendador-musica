package com.ejemplo.musicaemoji.controller;

import com.ejemplo.musicaemoji.model.EmojiMood;
import com.ejemplo.musicaemoji.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController // Indica que esta clase es un controlador REST
@RequestMapping("/api/recommendations") // Mapea todas las peticiones que empiezan por /api/recommendations
public class RecommendationController {

    @Autowired // Inyecta una instancia de RecommendationService
    private RecommendationService recommendationService;

    // Endpoint principal para obtener recomendaciones de música basado en una cadena de emojis
    // Ejemplo de uso: GET /api/recommendations/by-emojis?emojis=😄🎉
    @GetMapping("/by-emojis")
    public ResponseEntity<List<String>> getRecommendationsByEmojis(@RequestParam String emojis) {
        Set<String> recommendedGenres = recommendationService.recommendGenresByEmojis(emojis);
        List<String> recommendedSongs = recommendationService.getSampleSongsForGenres(recommendedGenres);
        return ResponseEntity.ok(recommendedSongs); // Devuelve la lista de canciones recomendadas
    }

    // --- Endpoints para gestionar los mapeos de EmojiMood (útil para un panel de administración) ---
    // Ejemplo de uso: GET /api/recommendations/emojimoods
    @GetMapping("/emojimoods")
    public List<EmojiMood> getAllEmojiMoods() {
        return recommendationService.getAllEmojiMoods();
    }

    // Ejemplo de uso: POST /api/recommendations/emojimoods
    // Cuerpo de la petición (JSON):
    // {
    //    "emoji": "😎",
    //    "moodDescription": "Genial",
    //    "genreHint": "Funk"
    // }
    @PostMapping("/emojimoods")
    public ResponseEntity<EmojiMood> createEmojiMood(@RequestBody EmojiMood emojiMood) {
        EmojiMood createdEmojiMood = recommendationService.createEmojiMood(emojiMood);
        return new ResponseEntity<>(createdEmojiMood, HttpStatus.CREATED); // Devuelve 201 Created
    }

    // Opcional: Puedes añadir endpoints para PUT y DELETE de EmojiMood si quieres una API CRUD completa para ellos
    @PutMapping("/emojimoods/{id}")
    public ResponseEntity<EmojiMood> updateEmojiMood(@PathVariable Long id, @RequestBody EmojiMood emojiMoodDetails) {
        try {
            EmojiMood updatedEmojiMood = recommendationService.updateEmojiMood(id, emojiMoodDetails);
            return ResponseEntity.ok(updatedEmojiMood);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/emojimoods/{id}")
    public ResponseEntity<Void> deleteEmojiMood(@PathVariable Long id) {
        recommendationService.deleteEmojiMood(id);
        return ResponseEntity.noContent().build();
    }
}