package com.ejemplo.musicaemoji.controller;

import com.ejemplo.musicaemoji.model.EmojiMood;
import com.ejemplo.musicaemoji.model.RecommendationResponse;
import com.ejemplo.musicaemoji.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * Endpoint para obtener recomendaciones de música basadas en emojis.
     * Recibe una cadena de emojis y devuelve una lista de canciones recomendadas
     * junto con los géneros asociados.
     *
     * @param emojis La cadena de emojis introducida por el usuario.
     * @return Un Mono que emite ResponseEntity con RecommendationResponse
     * que contiene la lista de SongDto y el conjunto de géneros recomendados.
     */
    @GetMapping("/recommendations/by-emojis")
    public Mono<ResponseEntity<RecommendationResponse>> getRecommendationsByEmojis(@RequestParam String emojis) {
        return recommendationService.getRecommendationsByEmojis(emojis)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // --- Endpoints para la gestión de EmojiMoods (CRUD) ---
    // Aunque no se usan directamente en la app Android actual, son útiles para la administración del backend.

    /**
     * Obtiene todos los mapeos de emojis a estados de ánimo.
     * @return Una lista de objetos EmojiMood.
     */
    @GetMapping("/emojimoods")
    public Mono<ResponseEntity<List<EmojiMood>>> getAllEmojiMoods() {
        return recommendationService.getAllEmojiMoods()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Crea un nuevo mapeo de emoji a estado de ánimo.
     * @param emojiMood El objeto EmojiMood a crear.
     * @return El objeto EmojiMood creado.
     */
    @PostMapping("/emojimoods")
    public ResponseEntity<EmojiMood> createEmojiMood(@RequestBody EmojiMood emojiMood) {
        // === CAMBIO AQUÍ: Usar .block() para obtener el resultado del Mono ===
        return ResponseEntity.ok(recommendationService.createEmojiMood(emojiMood).block());
    }

    /**
     * Actualiza un mapeo de emoji a estado de ánimo existente.
     * @param id El ID del EmojiMood a actualizar.
     * @param updatedEmojiMood Los datos actualizados del EmojiMood.
     * @return El objeto EmojiMood actualizado.
     */
    @PutMapping("/emojimoods/{id}")
    public ResponseEntity<EmojiMood> updateEmojiMood(@PathVariable Long id, @RequestBody EmojiMood updatedEmojiMood) {
        // === CAMBIO AQUÍ: Usar .block() para obtener el resultado del Mono ===
        return ResponseEntity.ok(recommendationService.updateEmojiMood(id, updatedEmojiMood).block());
    }

    /**
     * Elimina un mapeo de emoji a estado de ánimo.
     * @param id El ID del EmojiMood a eliminar.
     * @return ResponseEntity sin contenido si la eliminación fue exitosa.
     */
    @DeleteMapping("/emojimoods/{id}")
    public ResponseEntity<Void> deleteEmojiMood(@PathVariable Long id) {
        // === CAMBIO AQUÍ: Usar .block() para completar el Mono ===
        recommendationService.deleteEmojiMood(id).block();
        return ResponseEntity.noContent().build();
    }
}