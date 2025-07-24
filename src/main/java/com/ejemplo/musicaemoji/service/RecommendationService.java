package com.ejemplo.musicaemoji.service;

import com.ejemplo.musicaemoji.model.EmojiMood;
import com.ejemplo.musicaemoji.repository.EmojiMoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final EmojiMoodRepository emojiMoodRepository;
    private final SpotifyService spotifyService; // Inyecta SpotifyService

    @Autowired
    public RecommendationService(EmojiMoodRepository emojiMoodRepository, SpotifyService spotifyService) {
        this.emojiMoodRepository = emojiMoodRepository;
        this.spotifyService = spotifyService; // Asigna SpotifyService
    }

    @Transactional(readOnly = true)
    public List<EmojiMood> getAllEmojiMoods() {
        return emojiMoodRepository.findAll();
    }

    @Transactional
    public EmojiMood createEmojiMood(EmojiMood emojiMood) {
        return emojiMoodRepository.save(emojiMood);
    }

    @Transactional
    public EmojiMood updateEmojiMood(Long id, EmojiMood updatedEmojiMood) {
        return emojiMoodRepository.findById(id).map(emojiMood -> {
            emojiMood.setEmoji(updatedEmojiMood.getEmoji());
            emojiMood.setMoodDescription(updatedEmojiMood.getMoodDescription());
            emojiMood.setGenreHint(updatedEmojiMood.getGenreHint());
            return emojiMoodRepository.save(emojiMood);
        }).orElseThrow(() -> new RuntimeException("EmojiMood not found with id " + id));
    }

    @Transactional
    public void deleteEmojiMood(Long id) {
        emojiMoodRepository.deleteById(id);
    }

    /**
     * Recomienda géneros basados en los emojis de entrada.
     * @param emojisInput Cadena de emojis.
     * @return Conjunto de géneros recomendados.
     */
    public Set<String> recommendGenresByEmojis(String emojisInput) {
        Set<String> recommendedGenres = new HashSet<>();
        boolean directMatchFound = false;

        // Busca coincidencias directas de emojis con géneros
        emojisInput.codePoints().forEach(codePoint -> {
            String emoji = new String(Character.toChars(codePoint));
            emojiMoodRepository.findByEmoji(emoji).ifPresent(mood -> {
                recommendedGenres.add(mood.getGenreHint());
            });
        });

        if (!recommendedGenres.isEmpty()) {
            directMatchFound = true;
        }

        // Lógica de fallback si no se encuentran coincidencias directas o para emojis específicos
        if (!directMatchFound) {
            emojisInput.codePoints().forEach(codePoint -> {
                String emoji = new String(Character.toChars(codePoint));
                if ("🤷‍♀️".equals(emoji)) {
                    recommendedGenres.add("Indie");
                } else if ("🎉".equals(emoji)) {
                    recommendedGenres.add("Pop");
                    recommendedGenres.add("Dance");
                }
                // Puedes añadir más lógica de fallback aquí para otros emojis si es necesario
            });
        }

        // Si después de todo no hay géneros recomendados, sugiere "Indie" por defecto.
        if (recommendedGenres.isEmpty() && !emojisInput.isEmpty()) {
            recommendedGenres.add("Indie");
        }

        return recommendedGenres.stream()
                                .filter(genre -> !genre.isEmpty())
                                .collect(Collectors.toSet());
    }

    /**
     * Obtiene recomendaciones de canciones de Spotify para los géneros dados.
     * @param genres Conjunto de géneros para los que buscar canciones.
     * @return Lista de cadenas con el nombre de la canción, artista y URL de Spotify.
     */
    public List<String> getSpotifyRecommendationsForGenres(Set<String> genres) {
        List<String> allSongs = new ArrayList<>();
        int songsPerGenre = 5; // Número de canciones a buscar por cada género

        for (String genre : genres) {
            // Construye la query de búsqueda para Spotify
            String searchQuery = "genre:" + genre;

            // Llama a SpotifyService para buscar canciones
            // Usamos .blockOptional() para obtener el resultado de Mono de forma síncrona.
            // En una aplicación más compleja, podrías querer manejar esto de forma reactiva.
            List<String> genreSongs = spotifyService.searchSpotify(searchQuery, "track", songsPerGenre)
                                                    .blockOptional()
                                                    .orElse(Collections.emptyList());

            if (genreSongs.isEmpty()) {
                // Si no se encuentran canciones en Spotify para el género,
                // puedes añadir un fallback a canciones estáticas o un mensaje.
                System.out.println("No se encontraron canciones de Spotify para el género: " + genre + ". Usando fallback estático.");
                allSongs.addAll(getFallbackSongsForGenre(genre, songsPerGenre));
            } else {
                allSongs.addAll(genreSongs);
            }
        }
        // Eliminar duplicados si una canción aparece en múltiples búsquedas de género
        return allSongs.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Proporciona canciones de fallback estáticas si la búsqueda en Spotify falla o no devuelve resultados.
     * Esto es útil para asegurar que siempre haya alguna recomendación.
     * @param genre El género para el que se buscan canciones de fallback.
     * @param limit El número de canciones de fallback a devolver.
     * @return Lista de canciones de fallback.
     */
    private List<String> getFallbackSongsForGenre(String genre, int limit) {
        // Aquí puedes definir un mapa de canciones estáticas como las que tenías antes
        // para usar como respaldo. Por simplicidad, devolveremos un mensaje genérico.
        // En una aplicación real, tendrías un mapa grande como el que tenías en el método anterior.

        Map<String, List<String>> fallbackGenreSamples = new HashMap<>();
        // Puedes copiar aquí el mapa completo de canciones estáticas del método anterior
        // para tener un fallback robusto. Por ahora, un ejemplo simplificado:
        fallbackGenreSamples.put("Pop", Arrays.asList(
            "Blinding Lights - The Weeknd (Pop - Fallback)",
            "Shape of You - Ed Sheeran (Pop - Fallback)",
            "Uptown Funk - Mark Ronson ft. Bruno Mars (Pop - Fallback)"
        ));
        fallbackGenreSamples.put("Rock", Arrays.asList(
            "Bohemian Rhapsody - Queen (Rock - Fallback)",
            "Stairway to Heaven - Led Zeppelin (Rock - Fallback)",
            "Smells Like Teen Spirit - Nirvana (Rock - Fallback)"
        ));
        // ... añade más géneros de fallback aquí si lo deseas.

        // CORREGIDO: Eliminado el paréntesis extra al final de la línea.
        List<String> fallbackSongs = fallbackGenreSamples.getOrDefault(genre,
            Collections.singletonList("No hay recomendaciones de Spotify ni de fallback para " + genre));

        // Asegurarse de devolver solo el límite especificado
        return fallbackSongs.stream().limit(limit).collect(Collectors.toList());
    }
}