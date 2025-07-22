package com.ejemplo.musicaemoji.service;

import com.ejemplo.musicaemoji.model.EmojiMood;
import com.ejemplo.musicaemoji.repository.EmojiMoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importar Transactional

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final EmojiMoodRepository emojiMoodRepository;

    @Autowired
    public RecommendationService(EmojiMoodRepository emojiMoodRepository) {
        this.emojiMoodRepository = emojiMoodRepository;
    }

    @Transactional(readOnly = true) // Añadido para asegurar que la lectura se haga en un contexto transaccional
    public List<EmojiMood> getAllEmojiMoods() {
        return emojiMoodRepository.findAll();
    }

    @Transactional // Asegura que las operaciones de escritura se realicen en una transacción
    public EmojiMood createEmojiMood(EmojiMood emojiMood) {
        return emojiMoodRepository.save(emojiMood);
    }

    @Transactional // Asegura que las operaciones de escritura se realicen en una transacción
    public EmojiMood updateEmojiMood(Long id, EmojiMood updatedEmojiMood) {
        return emojiMoodRepository.findById(id).map(emojiMood -> {
            emojiMood.setEmoji(updatedEmojiMood.getEmoji());
            emojiMood.setMoodDescription(updatedEmojiMood.getMoodDescription());
            emojiMood.setGenreHint(updatedEmojiMood.getGenreHint());
            return emojiMoodRepository.save(emojiMood);
        }).orElseThrow(() -> new RuntimeException("EmojiMood not found with id " + id));
    }

    @Transactional // Asegura que las operaciones de escritura se realicen en una transacción
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

        // Canciones de Reggaeton (10 ejemplos) - ¡Añadido!
        genreSamples.put("Reggaeton", Arrays.asList(
            "Gasolina - Daddy Yankee (Reggaeton)",
            "Dura - Daddy Yankee (Reggaeton)",
            "Con Calma - Daddy Yankee & Snow (Reggaeton)",
            "Despacito - Luis Fonsi ft. Daddy Yankee (Reggaeton)", // También en Pop
            "Mi Gente - J Balvin & Willy William (Reggaeton)",
            "X - Nicky Jam & J Balvin (Reggaeton)",
            "Calma - Pedro Capó & Farruko (Reggaeton)",
            "Taki Taki - DJ Snake ft. Selena Gomez, Ozuna, Cardi B (Reggaeton)",
            "China - Anuel AA, Daddy Yankee, Karol G, Ozuna, J Balvin (Reggaeton)",
            "Safaera - Bad Bunny, Jowell & Randy, Ñengo Flow (Reggaeton)"
        ));

        // Canciones de Balada (10 ejemplos) - ¡Añadido!
        genreSamples.put("Balada", Arrays.asList(
            "Contigo en la distancia - Christina Aguilera (Balada)",
            "My Heart Will Go On - Celine Dion (Balada)",
            "I Will Always Love You - Whitney Houston (Balada)",
            "Unbreak My Heart - Toni Braxton (Balada)",
            "Hello - Adele (Balada)",
            "Someone Like You - Adele (Balada)",
            "All of Me - John Legend (Balada)",
            "Perfect - Ed Sheeran (Balada)",
            "Hallelujah - Leonard Cohen (Balada)",
            "Can't Help Falling in Love - Elvis Presley (Balada)"
        ));

        // Canciones de Electrónica (10 ejemplos) - ¡Añadido!
        genreSamples.put("Electrónica", Arrays.asList(
            "Strobe - Deadmau5 (Electrónica)",
            "Levels - Avicii (Electrónica)", // También en Dance
            "Animals - Martin Garrix (Electrónica)",
            "Clarity - Zedd ft. Foxes (Electrónica)",
            "Faded - Alan Walker (Electrónica)",
            "Alone - Marshmello (Electrónica)",
            "The Middle - Zedd, Maren Morris, Grey (Electrónica)",
            "Lean On - Major Lazer & DJ Snake (Electrónica)", // También en Dance
            "Where Are Ü Now - Skrillex & Diplo ft. Justin Bieber (Electrónica)", // También en Dance
            "Ghosts 'n' Stuff - Deadmau5 ft. Rob Swire (Electrónica)"
        ));

        // Canciones de Jazz (10 ejemplos) - ¡Añadido!
        genreSamples.put("Jazz", Arrays.asList(
            "Take Five - Dave Brubeck Quartet (Jazz)",
            "So What - Miles Davis (Jazz)",
            "My Favorite Things - John Coltrane (Jazz)",
            "What a Wonderful World - Louis Armstrong (Jazz)",
            "Fly Me to the Moon - Frank Sinatra (Jazz)",
            "Summertime - Ella Fitzgerald (Jazz)",
            "Giant Steps - John Coltrane (Jazz)",
            "All of Me - Billie Holiday (Jazz)",
            "Autumn Leaves - Cannonball Adderley (Jazz)",
            "Blue in Green - Miles Davis (Jazz)"
        ));

        // Canciones de R&B (10 ejemplos) - ¡Añadido!
        genreSamples.put("R&B", Arrays.asList(
            "Crazy in Love - Beyoncé ft. Jay-Z (R&B)",
            "No Scrubs - TLC (R&B)",
            "Waterfalls - TLC (R&B)",
            "Say My Name - Destiny's Child (R&B)",
            "U Remind Me - Usher (R&B)",
            "If I Ain't Got You - Alicia Keys (R&B)",
            "We Belong Together - Mariah Carey (R&B)",
            "Finesse - Bruno Mars ft. Cardi B (R&B)",
            "Leave The Door Open - Silk Sonic (R&B)",
            "Essence - Wizkid ft. Tems (R&B)"
        ));

        // Canciones de Clásica (10 ejemplos) - ¡Añadido!
        genreSamples.put("Clásica", Arrays.asList(
            "Sinfonía No. 5 - Ludwig van Beethoven (Clásica)",
            "Für Elise - Ludwig van Beethoven (Clásica)",
            "Eine kleine Nachtmusik - Wolfgang Amadeus Mozart (Clásica)",
            "Claro de Luna - Claude Debussy (Clásica)",
            "Las Cuatro Estaciones - Antonio Vivaldi (Clásica)",
            "Marcha Turca - Wolfgang Amadeus Mozart (Clásica)",
            "Ave Maria - Franz Schubert (Clásica)",
            "Canon en Re Mayor - Johann Pachelbel (Clásica)",
            "Concierto para piano No. 2 - Sergei Rachmaninoff (Clásica)",
            "O Fortuna (Carmina Burana) - Carl Orff (Clásica)"
        ));

        // Canciones de Ambient (10 ejemplos) - ¡Añadido!
        genreSamples.put("Ambient", Arrays.asList(
            "Ambient 1: Music for Airports - Brian Eno (Ambient)",
            "Weightless - Marconi Union (Ambient)",
            "Selected Ambient Works 85-92 - Aphex Twin (Ambient)",
            "An Ending (Ascent) - Brian Eno (Ambient)",
            "Reflection - Brian Eno (Ambient)",
            "Structures From Silence - Steve Roach (Ambient)",
            "The Pearl - Harold Budd & Brian Eno (Ambient)",
            "Lux - Brian Eno (Ambient)",
            "Deep Blue - Biosphere (Ambient)",
            "Microgravity - The Orb (Ambient)"
        ));

        // Canciones de K-Pop (10 ejemplos) - ¡Añadido!
        genreSamples.put("K-Pop", Arrays.asList(
            "Dynamite - BTS (K-Pop)",
            "DDU-DU DDU-DU - BLACKPINK (K-Pop)",
            "Gangnam Style - PSY (K-Pop)",
            "God's Menu - Stray Kids (K-Pop)",
            "Psycho - Red Velvet (K-Pop)",
            "Love Shot - EXO (K-Pop)",
            "Feel Special - TWICE (K-Pop)",
            "Boy With Luv - BTS ft. Halsey (K-Pop)",
            "Kill This Love - BLACKPINK (K-Pop)",
            "Gee - Girls' Generation (K-Pop)"
        ));

        // Canciones de Heavy Metal (10 ejemplos) - ¡Añadido!
        genreSamples.put("Heavy Metal", Arrays.asList(
            "Master of Puppets - Metallica (Heavy Metal)",
            "Iron Man - Black Sabbath (Heavy Metal)",
            "Paranoid - Black Sabbath (Heavy Metal)",
            "Enter Sandman - Metallica (Heavy Metal)",
            "Holy Wars... The Punishment Due - Megadeth (Heavy Metal)",
            "Hallowed Be Thy Name - Iron Maiden (Heavy Metal)",
            "Ace of Spades - Motörhead (Heavy Metal)",
            "War Pigs - Black Sabbath (Heavy Metal)",
            "Breaking the Law - Judas Priest (Heavy Metal)",
            "Run to the Hills - Iron Maiden (Heavy Metal)"
        ));


        for (String genre : genres) {
            songs.addAll(genreSamples.getOrDefault(genre, Collections.emptyList()));
        }

        // Eliminar duplicados si un género se solicita varias veces
        return songs.stream().distinct().collect(Collectors.toList());
    }
}