package com.ejemplo.musicaemoji.service;

import com.ejemplo.musicaemoji.model.EmojiMood;
import com.ejemplo.musicaemoji.model.SongDto; // Importa SongDto
import com.ejemplo.musicaemoji.repository.EmojiMoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final EmojiMoodRepository emojiMoodRepository;
    private final SpotifyService spotifyService;

    @Autowired
    public RecommendationService(EmojiMoodRepository emojiMoodRepository, SpotifyService spotifyService) {
        this.emojiMoodRepository = emojiMoodRepository;
        this.spotifyService = spotifyService;
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

    public Set<String> recommendGenresByEmojis(String emojisInput) {
        Set<String> recommendedGenres = new HashSet<>();
        boolean directMatchFound = false;

        emojisInput.codePoints().forEach(codePoint -> {
            String emoji = new String(Character.toChars(codePoint));
            emojiMoodRepository.findByEmoji(emoji).ifPresent(mood -> {
                recommendedGenres.add(mood.getGenreHint());
            });
        });

        if (!recommendedGenres.isEmpty()) {
            directMatchFound = true;
        }

        if (!directMatchFound) {
            emojisInput.codePoints().forEach(codePoint -> {
                String emoji = new String(Character.toChars(codePoint));
                if ("🤷‍♀️".equals(emoji)) {
                    recommendedGenres.add("Indie");
                } else if ("🎉".equals(emoji)) {
                    recommendedGenres.add("Pop");
                    recommendedGenres.add("Dance");
                }
            });
        }

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
     * @return Lista de SongDto con los detalles de la canción.
     */
    public List<SongDto> getSpotifyRecommendationsForGenres(Set<String> genres) { // CAMBIO: Retorna List<SongDto>
        List<SongDto> allSongs = new ArrayList<>(); // CAMBIO: Lista de SongDto
        int songsPerGenre = 10;

        for (String genre : genres) {
            String searchQuery = "genre:" + genre;

            List<SongDto> genreSongs = spotifyService.searchSpotify(searchQuery, "track", songsPerGenre)
                                                    .blockOptional()
                                                    .orElse(Collections.emptyList());

            if (genreSongs.isEmpty()) {
                System.out.println("No se encontraron canciones de Spotify para el género: " + genre + ". Usando fallback estático.");
                allSongs.addAll(getFallbackSongsForGenre(genre, songsPerGenre));
            } else {
                allSongs.addAll(genreSongs);
            }
        }
        return allSongs.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Proporciona canciones de fallback estáticas si la búsqueda en Spotify falla o no devuelve resultados.
     * @param genre El género para el que se buscan canciones de fallback.
     * @param limit El número de canciones de fallback a devolver.
     * @return Lista de SongDto de fallback.
     */
    private List<SongDto> getFallbackSongsForGenre(String genre, int limit) { // CAMBIO: Retorna List<SongDto>
        Map<String, List<SongDto>> fallbackGenreSamples = new HashMap<>();

        // Ejemplos de fallback con SongDto (puedes expandir esto con más géneros y URLs reales si lo deseas)
        fallbackGenreSamples.put("Pop", Arrays.asList(
            new SongDto("Blinding Lights", "The Weeknd", "https://open.spotify.com/track/3PjlD4B4o4J4J4J4J4J4J4", ""),
            new SongDto("Shape of You", "Ed Sheeran", "https://open.spotify.com/track/7qiZfU4dY1lWllzX7pLGYa", ""),
            new SongDto("Uptown Funk", "Mark Ronson ft. Bruno Mars", "https://open.spotify.com/track/32OlwWuMpZ6b0aN2RZOeMS", "")
        ));
        fallbackGenreSamples.put("Rock", Arrays.asList(
            new SongDto("Bohemian Rhapsody", "Queen", "https://open.spotify.com/track/7tFiyTwD0FpgFfppXclCzo", ""),
            new SongDto("Stairway to Heaven", "Led Zeppelin", "https://open.spotify.com/track/5Pz0y30Jp4J4J4J4J4J4J4", ""),
            new SongDto("Smells Like Teen Spirit", "Nirvana", "https://open.spotify.com/track/4jC5S555555555555555555", "")
        ));
        // ... añade más géneros de fallback aquí si lo deseas.

        List<SongDto> fallbackSongs = fallbackGenreSamples.getOrDefault(genre,
            Collections.singletonList(new SongDto("No hay recomendaciones", "N/A", "", "")));

        return fallbackSongs.stream().limit(limit).collect(Collectors.toList());
    }
}