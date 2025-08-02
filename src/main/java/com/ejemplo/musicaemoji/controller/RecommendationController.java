package com.ejemplo.musicaemoji.controller;

import com.ejemplo.musicaemoji.model.EmojiMood;
import com.ejemplo.musicaemoji.model.RecommendationResponse;
import com.ejemplo.musicaemoji.model.SongDto;
import com.ejemplo.musicaemoji.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Autowired
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * Endpoint para obtener recomendaciones de música basadas en emojis.
     * Recibe una cadena de emojis y devuelve una lista de SongDto y el conjunto de géneros recomendados.
     * @param emojis La cadena de emojis introducida por el usuario.
     * @return ResponseEntity con RecommendationResponse.
     */
    @GetMapping("/recommendations/by-emojis")
    public ResponseEntity<RecommendationResponse> getRecommendationsByEmojis(@RequestParam String emojis) {
        Set<String> genres = recommendationService.recommendGenresByEmojis(emojis);
        List<SongDto> recommendations = recommendationService.getSpotifyRecommendationsForGenres(genres);

        // Asegúrate de que el constructor de RecommendationResponse es (Set<String> genres, List<SongDto> songs)
        RecommendationResponse response = new RecommendationResponse(genres, recommendations);
        return ResponseEntity.ok(response);
    }

    // --- Métodos CRUD para EmojiMood (Adaptados para Firestore) ---

    /**
     * Obtiene todos los mapeos de emoji a estado de ánimo.
     * @return Lista de todos los objetos EmojiMood.
     */
    @GetMapping("/emojimoods")
    public ResponseEntity<List<EmojiMood>> getAllEmojiMoods() {
        List<EmojiMood> emojiMoods = recommendationService.getAllEmojiMoods();
        return ResponseEntity.ok(emojiMoods);
    }

    /**
     * Obtiene un mapeo de emoji a estado de ánimo por su ID.
     * @param id El ID del EmojiMood.
     * @return El objeto EmojiMood si se encuentra.
     */
    @GetMapping("/emojimoods/{id}")
    public ResponseEntity<EmojiMood> getEmojiMoodById(@PathVariable String id) { // ID es String
        return recommendationService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crea un nuevo mapeo de emoji a estado de ánimo.
     * @param emojiMood El objeto EmojiMood a crear.
     * @return El objeto EmojiMood creado.
     */
    @PostMapping("/emojimoods")
    public ResponseEntity<EmojiMood> createEmojiMood(@RequestBody EmojiMood emojiMood) {
        EmojiMood createdEmojiMood = recommendationService.createEmojiMood(emojiMood);
        return ResponseEntity.ok(createdEmojiMood);
    }

    /**
     * Actualiza un mapeo de emoji a estado de ánimo existente.
     * @param id El ID del EmojiMood a actualizar.
     * @param updatedEmojiMood Los datos actualizados del EmojiMood.
     * @return El objeto EmojiMood actualizado.
     */
    @PutMapping("/emojimoods/{id}")
    public ResponseEntity<EmojiMood> updateEmojiMood(@PathVariable String id, @RequestBody EmojiMood updatedEmojiMood) { // ID es String
        EmojiMood result = recommendationService.updateEmojiMood(id, updatedEmojiMood);
        return ResponseEntity.ok(result);
    }

    /**
     * Elimina un mapeo de emoji a estado de ánimo.
     * @param id El ID del EmojiMood a eliminar.
     * @return ResponseEntity sin contenido si la eliminación fue exitosa.
     */
    @DeleteMapping("/emojimoods/{id}")
    public ResponseEntity<Void> deleteEmojiMood(@PathVariable String id) { // ID es String
        recommendationService.deleteEmojiMood(id);
        return ResponseEntity.noContent().build();
    }
}