package com.ejemplo.musicaemoji.service;

import com.ejemplo.musicaemoji.model.EmojiMood;
import com.ejemplo.musicaemoji.repository.EmojiMoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final EmojiMoodRepository emojiMoodRepository;

    @Autowired
    public RecommendationService(EmojiMoodRepository emojiMoodRepository) {
        this.emojiMoodRepository = emojiMoodRepository;
    }

    public List<EmojiMood> getAllEmojiMoods() {
        return emojiMoodRepository.findAll();
    }

    public EmojiMood createEmojiMood(EmojiMood emojiMood) {
        return emojiMoodRepository.save(emojiMood);
    }

    public EmojiMood updateEmojiMood(Long id, EmojiMood updatedEmojiMood) {
        return emojiMoodRepository.findById(id).map(emojiMood -> {
            emojiMood.setEmoji(updatedEmojiMood.getEmoji());
            emojiMood.setMoodDescription(updatedEmojiMood.getMoodDescription());
            emojiMood.setGenreHint(updatedEmojiMood.getGenreHint());
            return emojiMoodRepository.save(emojiMood);
        }).orElseThrow(() -> new RuntimeException("EmojiMood not found with id " + id));
    }

    public void deleteEmojiMood(Long id) {
        emojiMoodRepository.deleteById(id);
    }

    public Set<String> recommendGenresByEmojis(String emojisInput) {
        Set<String> recommendedGenres = new HashSet<>();
        boolean directMatchFound = false; // Bandera para saber si se encontró alguna coincidencia directa

        // Primera pasada: Buscar coincidencias directas en la base de datos para cada emoji.
        emojisInput.codePoints().forEach(codePoint -> {
            String emoji = new String(Character.toChars(codePoint));
            emojiMoodRepository.findByEmoji(emoji).ifPresent(mood -> {
                recommendedGenres.add(mood.getGenreHint());
            });
        });

        // Si se encontraron géneros en la primera pasada, actualiza la bandera
        if (!recommendedGenres.isEmpty()) {
            directMatchFound = true;
        }

        // Segunda pasada: Aplicar lógica de "fallback" específica
        // SOLO SI NO se encontraron géneros directamente en la primera pasada.
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

        // Fallback general: Si después de todas las búsquedas (directas y específicas),
        // aún no se ha recomendado ningún género Y la entrada no estaba vacía.
        if (recommendedGenres.isEmpty() && !emojisInput.isEmpty()) {
            recommendedGenres.add("Indie"); // Género por defecto si no se encuentra nada
        }

        // Filtra cualquier posible cadena vacía y asegura que no haya duplicados
        return recommendedGenres.stream()
                                .filter(genre -> !genre.isEmpty())
                                .collect(Collectors.toSet());
    }

    // Método para obtener canciones de ejemplo basado en géneros
    public List<String> getSampleSongsForGenres(Set<String> genres) {
        List<String> songs = new ArrayList<>();
        Map<String, List<String>> genreSamples = new HashMap<>();

        // Canciones de Pop (2)
        genreSamples.put("Pop", Arrays.asList("Song A - Artist 1 (Pop)", "Song B - Artist 2 (Pop)"));
        // Canciones de Rock (2)
        genreSamples.put("Rock", Arrays.asList("Song C - Artist 3 (Rock)", "Song D - Artist 4 (Rock)"));
        // Canciones de Dance (1)
        genreSamples.put("Dance", Collections.singletonList("Song E - Artist 5 (Dance)"));
        // Canciones de Blues (1)
        genreSamples.put("Blues", Collections.singletonList("Song F - Artist 6 (Blues)"));
        // Canciones de Hip Hop (1)
        genreSamples.put("Hip Hop", Collections.singletonList("Song G - Artist 7 (Hip Hop)"));
        // Canciones de Indie (1)
        genreSamples.put("Indie", Collections.singletonList("Song J - Artist 10 (Indie)"));
        // Puedes añadir más géneros y canciones aquí

        for (String genre : genres) {
            songs.addAll(genreSamples.getOrDefault(genre, Collections.emptyList()));
        }

        // Eliminar duplicados si un género se solicita varias veces
        return songs.stream().distinct().collect(Collectors.toList());
    }
}