package com.ejemplo.musicaemoji.service;

import com.ejemplo.musicaemoji.model.EmojiMood;
import com.ejemplo.musicaemoji.model.RecommendationResponse;
import com.ejemplo.musicaemoji.model.SongDto; // Importa SongDto
import com.ejemplo.musicaemoji.repository.EmojiMoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono; // Importar Mono

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Arrays; // Importar para Arrays
import java.util.Map; // Importar para Map
import java.util.HashMap; // Importar para HashMap
import java.util.ArrayList; // Importar para ArrayList


@Service
public class RecommendationService {

    private final EmojiMoodRepository emojiMoodRepository;
    private final SpotifyService spotifyService;

    @Autowired
    public RecommendationService(EmojiMoodRepository emojiMoodRepository, SpotifyService spotifyService) {
        this.emojiMoodRepository = emojiMoodRepository;
        this.spotifyService = spotifyService;
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
    @SuppressWarnings("unchecked")
    public Mono<RecommendationResponse> getRecommendationsByEmojis(String emojis) {
        if (emojis == null || emojis.trim().isEmpty()) {
            // Especificar tipos genéricos para emptyList y emptySet
            return Mono.just(new RecommendationResponse(Collections.<SongDto>emptyList(), Collections.<String>emptySet()));
        }

        Set<String> recommendedGenres = new HashSet<>();
        for (char emojiChar : emojis.toCharArray()) {
            String emojiString = String.valueOf(emojiChar);
            emojiMoodRepository.findByEmoji(emojiString).ifPresent(emojiMood ->
                recommendedGenres.add(emojiMood.getGenreHint())
            );
        }

        // Lógica de fallback específica si no se encontraron géneros directamente en la base de datos
        if (recommendedGenres.isEmpty()) {
            for (char emojiChar : emojis.toCharArray()) {
                String emoji = String.valueOf(emojiChar);
                if ("🤷‍♀️".equals(emoji)) {
                    recommendedGenres.add("Indie");
                } else if ("🎉".equals(emoji)) {
                    recommendedGenres.add("Pop");
                    recommendedGenres.add("Dance");
                }
                // Puedes añadir más lógica de fallback aquí para otros emojis si es necesario
            }
        }

        // Fallback general: Si después de todas las búsquedas, aún no se ha recomendado ningún género
        if (recommendedGenres.isEmpty() && !emojis.isEmpty()) {
            recommendedGenres.add("Indie"); // Género por defecto si no se encuentra nada
        }

        // Filtra cualquier posible cadena vacía y asegura que no haya duplicados
        Set<String> finalRecommendedGenres = recommendedGenres.stream()
                                .filter(genre -> !genre.isEmpty())
                                .collect(Collectors.toSet());

        if (finalRecommendedGenres.isEmpty()) {
            return Mono.just(new RecommendationResponse(Collections.<SongDto>emptyList(), Collections.<String>emptySet()));
        }

        // Iteramos sobre los géneros para hacer una búsqueda por cada uno y combinamos los resultados.
        List<Mono<List<SongDto>>> spotifyCalls = finalRecommendedGenres.stream()
                .map(genre -> spotifyService.searchSpotify("genre:" + genre, "track", 10, "ES", genre)) // Pasa el género como genreHint
                .collect(Collectors.toList());

        // Combinar los resultados de todas las llamadas a Spotify
        return Mono.zip(spotifyCalls, results -> {
            List<SongDto> allSongs = new ArrayList<>();
            for (Object result : results) {
                if (result instanceof List) {
                    // === CORRECCIÓN AQUÍ: Cast seguro con comprobación de tipo ===
                    allSongs.addAll((List<SongDto>) result);
                }
            }
            // Eliminar duplicados si una canción aparece en múltiples búsquedas de género
            List<SongDto> distinctSongs = allSongs.stream().distinct().collect(Collectors.toList());

            // Si no se encontraron canciones en Spotify, usar el fallback estático
            if (distinctSongs.isEmpty()) {
                System.out.println("No se encontraron canciones de Spotify para los géneros: " + finalRecommendedGenres + ". Usando fallback estático.");
                distinctSongs.addAll(getFallbackSongsForGenres(finalRecommendedGenres, 10)); // Obtener 10 canciones de fallback en total
            }

            return new RecommendationResponse(distinctSongs, finalRecommendedGenres);
        }).onErrorResume(e -> {
            System.err.println("Error general al obtener recomendaciones: " + e.getMessage());
            // En caso de error, devolver un RecommendationResponse con la lista de géneros y canciones de fallback
            List<SongDto> fallbackSongs = getFallbackSongsForGenres(finalRecommendedGenres, 10);
            return Mono.just(new RecommendationResponse(fallbackSongs, finalRecommendedGenres));
        });
    }


    /**
     * Proporciona canciones de fallback estáticas si la búsqueda en Spotify falla o no devuelve resultados.
     * Esto es útil para asegurar que siempre haya alguna recomendación.
     * @param genres El conjunto de géneros para el que se buscan canciones de fallback.
     * @param limit El número de canciones de fallback a devolver por género.
     * @return Lista de SongDto de fallback.
     */
    private List<SongDto> getFallbackSongsForGenres(Set<String> genres, int limit) {
        List<SongDto> allFallbackSongs = new ArrayList<>();
        Map<String, List<SongDto>> fallbackGenreSamples = new HashMap<>();

        // Ejemplos de fallback con SongDto (puedes expandir esto con más géneros y URLs reales si lo deseas)
        fallbackGenreSamples.put("Pop", Arrays.asList(
            new SongDto("Blinding Lights", "The Weeknd", "https://open.spotify.com/track/3PjlD4dY1lWllzX7pLGYa", "https://p.scdn.co/mp3-preview/32d0f0d2c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Pop"),
            new SongDto("Shape of You", "Ed Sheeran", "https://open.spotify.com/track/7qiZfU4dY1lWllzX7pLGYa", "https://p.scdn.co/mp3-preview/7d0f0d2c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e8", "Pop"),
            new SongDto("Uptown Funk", "Mark Ronson ft. Bruno Mars", "https://open.spotify.com/track/32OlwWuMpZ6b0aN2RZOeMS", "https://p.scdn.co/mp3-preview/2d0f0d2c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e8", "Pop")
        ));
        fallbackGenreSamples.put("Rock", Arrays.asList(
            new SongDto("Bohemian Rhapsody", "Queen", "https://open.spotify.com/track/7tFiyTwD0Ss15ZlVfVrFzb", "https://p.scdn.co/mp3-preview/1d0f0d2c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e8", "Rock"),
            new SongDto("Stairway to Heaven", "Led Zeppelin", "https://open.spotify.com/track/5Pz0y30Jp4J4J4J4J4J4J4", "https://p.scdn.co/mp3-preview/4d0f0d2c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e8", "Rock"),
            new SongDto("Smells Like Teen Spirit", "Nirvana", "https://open.spotify.com/track/4jC5S555555555555555555", "https://p.scdn.co/mp3-preview/6d0f0d2c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e8", "Rock")
        ));
        // ... (Añade aquí el resto de tus géneros de fallback con SongDto de 5 argumentos)

        for (String genre : genres) {
            List<SongDto> genreFallback = fallbackGenreSamples.getOrDefault(genre, Collections.emptyList());
            allFallbackSongs.addAll(genreFallback.stream().limit(limit).collect(Collectors.toList()));
        }

        return allFallbackSongs.stream().distinct().collect(Collectors.toList());
    }


    // --- Métodos CRUD para EmojiMood (Restaurados y ajustados a Mono) ---
    // Estos métodos son los que tu RecommendationController espera.

    @Transactional(readOnly = true)
    public Mono<List<EmojiMood>> getAllEmojiMoods() {
        return Mono.fromCallable(() -> emojiMoodRepository.findAll());
    }

    @Transactional
    public Mono<EmojiMood> createEmojiMood(EmojiMood emojiMood) {
        return Mono.fromCallable(() -> emojiMoodRepository.save(emojiMood));
    }

    @Transactional
    public Mono<EmojiMood> updateEmojiMood(Long id, EmojiMood updatedEmojiMood) {
        return Mono.fromCallable(() -> emojiMoodRepository.findById(id).map(emojiMood -> {
            emojiMood.setEmoji(updatedEmojiMood.getEmoji());
            emojiMood.setMoodDescription(updatedEmojiMood.getMoodDescription());
            emojiMood.setGenreHint(updatedEmojiMood.getGenreHint());
            return emojiMoodRepository.save(emojiMood);
        }).orElseThrow(() -> new RuntimeException("EmojiMood not found with id " + id)));
    }

    @Transactional
    public Mono<Void> deleteEmojiMood(Long id) {
        return Mono.fromRunnable(() -> emojiMoodRepository.deleteById(id)).then();
    }
}