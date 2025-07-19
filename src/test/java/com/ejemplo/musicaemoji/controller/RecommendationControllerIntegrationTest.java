package com.ejemplo.musicaemoji.controller;

import com.ejemplo.musicaemoji.model.EmojiMood;
import com.ejemplo.musicaemoji.repository.EmojiMoodRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.endsWith;

@SpringBootTest // Carga el contexto completo de Spring Boot
@AutoConfigureMockMvc // Configura MockMvc
class RecommendationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmojiMoodRepository emojiMoodRepository; // Para interactuar directamente con la BD de prueba

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        emojiMoodRepository.deleteAll(); // Limpia la base de datos antes de cada test
        // Carga algunos datos iniciales para las pruebas
        emojiMoodRepository.save(new EmojiMood(null, "😄", "Alegre", "Pop"));
        // ¡IMPORTANTE! Hemos eliminado esta línea para que el test de fallback de 🎉 funcione
        // emojiMoodRepository.save(new EmojiMood(null, "🎉", "Fiesta", "Dance"));
        emojiMoodRepository.save(new EmojiMood(null, "😢", "Triste", "Blues"));
        emojiMoodRepository.save(new EmojiMood(null, "💪", "Fuerza", "Rock"));
        emojiMoodRepository.save(new EmojiMood(null, "🚀", "Movimiento", "Hip Hop"));
    }

    @Test
    void getRecommendationsByEmojis_singleEmojiMatch() throws Exception {
        mockMvc.perform(get("/api/recommendations/by-emojis?emojis=😄"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2))) // Esperamos 2 canciones de Pop del getSampleSongsForGenres
                .andExpect(jsonPath("$[0]").value("Song A - Artist 1 (Pop)"));
    }

    @Test
    void getRecommendationsByEmojis_multipleEmojisMatch() throws Exception {
        mockMvc.perform(get("/api/recommendations/by-emojis?emojis=😄💪"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(4))) // 2 Pop + 2 Rock
                .andExpect(jsonPath("$", hasItem(endsWith("(Pop)")))) // Verifica que haya canciones de Pop
                .andExpect(jsonPath("$", hasItem(endsWith("(Rock)")))); // Verifica que haya canciones de Rock
    }

    @Test
    void getRecommendationsByEmojis_noDirectMatch_usesFallback() throws Exception {
        mockMvc.perform(get("/api/recommendations/by-emojis?emojis=🤷‍♀️")) // Emoji que no está en la BD
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasItem(endsWith("(Indie)")))); // Espera una recomendación de "Indie" (fallback general)
    }

    @Test
    void getRecommendationsByEmojis_fallbackForSpecificEmojiPresent() throws Exception {
        mockMvc.perform(get("/api/recommendations/by-emojis?emojis=🎉")) // Emoji que tiene un fallback específico
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasItem(endsWith("(Pop)"))))
                .andExpect(jsonPath("$", hasItem(endsWith("(Dance)"))));
    }


    @Test
    void getAllEmojiMoods_shouldReturnAllConfiguredEmojis() throws Exception {
        mockMvc.perform(get("/api/recommendations/emojimoods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(4))) // Ahora son 4 emojis los que insertamos en BeforeEach
                .andExpect(jsonPath("$[0].emoji").value("😄")); // Se elimina la aserción del segundo emoji ya que el orden puede variar
    }

    @Test
    void createEmojiMood_shouldReturnCreatedEmojiMood() throws Exception {
        EmojiMood newEmojiMood = new EmojiMood(null, "🥳", "Celebración", "Reggaeton");
        mockMvc.perform(post("/api/recommendations/emojimoods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEmojiMood)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.emoji").value("🥳"))
                .andExpect(jsonPath("$.moodDescription").value("Celebración"))
                .andExpect(jsonPath("$.genreHint").value("Reggaeton"));
    }

    @Test
    void updateEmojiMood_shouldReturnUpdatedEmojiMood() throws Exception {
        EmojiMood existingMood = emojiMoodRepository.save(new EmojiMood(null, "🤔", "Pensativo", "Jazz"));
        EmojiMood updatedDetails = new EmojiMood(null, "🤔", "Reflexivo", "Lo-fi");

        mockMvc.perform(put("/api/recommendations/emojimoods/" + existingMood.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moodDescription").value("Reflexivo"))
                .andExpect(jsonPath("$.genreHint").value("Lo-fi"));
    }

    @Test
    void deleteEmojiMood_shouldDeleteEmojiMood() throws Exception {
        EmojiMood moodToDelete = emojiMoodRepository.save(new EmojiMood(null, "🗑️", "Borrar", "None"));
        mockMvc.perform(delete("/api/recommendations/emojimoods/" + moodToDelete.getId()))
                .andExpect(status().isNoContent());
    }
}