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

        // Primera pasada: Buscar coincidencias directas en la base de datos para cada emoji.
        emojisInput.codePoints().forEach(codePoint -> {
            String emoji = new String(Character.toChars(codePoint));
            emojiMoodRepository.findByEmoji(emoji).ifPresent(mood -> {
                recommendedGenres.add(mood.getGenreHint());
            });
        });

        // Segunda pasada: Aplicar lógica de "fallback" específica si NO se encontraron géneros directamente.
        // Esto asegura que los fallbacks específicos (como para 🤷‍♀️ y 🎉) se consideren si no hay match directo.
        if (recommendedGenres.isEmpty()) {
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
        // aún no se ha recomendado ningún género y la entrada no estaba vacía.
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

        // Canciones de Pop (10 ejemplos)
        genreSamples.put("Pop", Arrays.asList(
            "Blinding Lights - The Weeknd (Pop)",
            "Shape of You - Ed Sheeran (Pop)",
            "Uptown Funk - Mark Ronson ft. Bruno Mars (Pop)",
            "Bad Guy - Billie Eilish (Pop)",
            "Despacito - Luis Fonsi ft. Daddy Yankee (Pop)",
            "Dance Monkey - Tones and I (Pop)",
            "Rolling in the Deep - Adele (Pop)",
            "Happy - Pharrell Williams (Pop)",
            "Old Town Road - Lil Nas X ft. Billy Ray Cyrus (Pop)",
            "Levitating - Dua Lipa (Pop)"
        ));

        // Canciones de Rock (10 ejemplos)
        genreSamples.put("Rock", Arrays.asList(
            "Bohemian Rhapsody - Queen (Rock)",
            "Stairway to Heaven - Led Zeppelin (Rock)",
            "Smells Like Teen Spirit - Nirvana (Rock)",
            "Sweet Child o' Mine - Guns N' Roses (Rock)",
            "Hotel California - Eagles (Rock)",
            "Livin' on a Prayer - Bon Jovi (Rock)",
            "Thunderstruck - AC/DC (Rock)",
            "Seven Nation Army - The White Stripes (Rock)",
            "Wonderwall - Oasis (Rock)",
            "Dream On - Aerosmith (Rock)"
        ));

        // Canciones de Dance (10 ejemplos)
        genreSamples.put("Dance", Arrays.asList(
            "Titanium - David Guetta ft. Sia (Dance)",
            "Wake Me Up - Avicii (Dance)",
            "Levels - Avicii (Dance)",
            "Lean On - Major Lazer & DJ Snake ft. MØ (Dance)",
            "Where Are Ü Now - Skrillex & Diplo ft. Justin Bieber (Dance)",
            "One Kiss - Calvin Harris & Dua Lipa (Dance)",
            "The Nights - Avicii (Dance)",
            "Don't You Worry Child - Swedish House Mafia ft. John Martin (Dance)",
            "I Gotta Feeling - The Black Eyed Peas (Dance)",
            "Closer - The Chainsmokers ft. Halsey (Dance)"
        ));

        // Canciones de Blues (10 ejemplos)
        genreSamples.put("Blues", Arrays.asList(
            "The Thrill Is Gone - B.B. King (Blues)",
            "Cross Road Blues - Robert Johnson (Blues)",
            "Sweet Home Chicago - Robert Johnson (Blues)",
            "Mannish Boy - Muddy Waters (Blues)",
            "Boom Boom - John Lee Hooker (Blues)",
            "Pride and Joy - Stevie Ray Vaughan (Blues)",
            "Red House - Jimi Hendrix (Blues)",
            "Born Under a Bad Sign - Albert King (Blues)",
            "Hoochie Coochie Man - Muddy Waters (Blues)",
            "Stormy Monday - T-Bone Walker (Blues)"
        ));

        // Canciones de Hip Hop (10 ejemplos)
        genreSamples.put("Hip Hop", Arrays.asList(
            "Lose Yourself - Eminem (Hip Hop)",
            "Still D.R.E. - Dr. Dre ft. Snoop Dogg (Hip Hop)",
            "N.Y. State of Mind - Nas (Hip Hop)",
            "California Love - 2Pac ft. Dr. Dre (Hip Hop)",
            "In Da Club - 50 Cent (Hip Hop)",
            "Hotline Bling - Drake (Hip Hop)",
            "God's Plan - Drake (Hip Hop)",
            "Sicko Mode - Travis Scott (Hip Hop)",
            "Mo Bamba - Sheck Wes (Hip Hop)",
            "Old Town Road - Lil Nas X (Hip Hop)" // Puede aparecer en Pop también
        ));

        // Canciones de Indie (10 ejemplos)
        genreSamples.put("Indie", Arrays.asList(
            "Do I Wanna Know? - Arctic Monkeys (Indie)",
            "R U Mine? - Arctic Monkeys (Indie)",
            "Chasing Cars - Snow Patrol (Indie)",
            "Ho Hey - The Lumineers (Indie)",
            "Little Talks - Of Monsters and Men (Indie)",
            "Pumped Up Kicks - Foster the People (Indie)",
            "Sweater Weather - The Neighbourhood (Indie)",
            "Take Me Out - Franz Ferdinand (Indie)",
            "Mr. Brightside - The Killers (Indie)",
            "Somebody That I Used to Know - Gotye ft. Kimbra (Indie)"
        ));

        // Puedes añadir más géneros y canciones aquí

        for (String genre : genres) {
            songs.addAll(genreSamples.getOrDefault(genre, Collections.emptyList()));
        }

        // Eliminar duplicados si un género se solicita varias veces
        return songs.stream().distinct().collect(Collectors.toList());
    }
}