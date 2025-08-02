package com.ejemplo.musicaemoji.service;

import com.ejemplo.musicaemoji.model.EmojiMood;
import com.ejemplo.musicaemoji.model.SongDto;
import com.ejemplo.musicaemoji.repository.EmojiMoodFirestoreRepository; // Importa el nuevo repositorio
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final EmojiMoodFirestoreRepository emojiMoodRepository; // Usa el nuevo repositorio
    private final SpotifyService spotifyService;

    @Autowired
    public RecommendationService(EmojiMoodFirestoreRepository emojiMoodRepository, SpotifyService spotifyService) {
        this.emojiMoodRepository = emojiMoodRepository;
        this.spotifyService = spotifyService;
    }

    // Métodos CRUD básicos para EmojiMood (si los necesitas, adaptados para Firestore)
    public List<EmojiMood> getAllEmojiMoods() {
        return emojiMoodRepository.findAll();
    }

    // === CORRECCIÓN AQUÍ: Añadido el método findById para el controlador ===
    public Optional<EmojiMood> findById(String id) {
        return emojiMoodRepository.findById(id);
    }
    // ======================================================================

    public EmojiMood createEmojiMood(EmojiMood emojiMood) {
        return emojiMoodRepository.save(emojiMood);
    }

    public EmojiMood updateEmojiMood(String id, EmojiMood updatedEmojiMood) { // ID es String
        return emojiMoodRepository.findById(id).map(existingEmojiMood -> {
            existingEmojiMood.setEmoji(updatedEmojiMood.getEmoji());
            existingEmojiMood.setMoodDescription(updatedEmojiMood.getMoodDescription());
            existingEmojiMood.setGenreHint(updatedEmojiMood.getGenreHint());
            return emojiMoodRepository.save(existingEmojiMood);
        }).orElseThrow(() -> new RuntimeException("EmojiMood not found with id " + id));
    }

    public void deleteEmojiMood(String id) { // ID es String
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

    public List<SongDto> getSpotifyRecommendationsForGenres(Set<String> genres) {
        List<SongDto> allSongs = new ArrayList<>();
        int songsPerGenre = 10;

        for (String genre : genres) {
            String searchQuery = "genre:" + genre;

            // Pasa el genreHint a searchSpotify
            List<SongDto> genreSongs = spotifyService.searchSpotify(searchQuery, "track", songsPerGenre, genre)
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

    private List<SongDto> getFallbackSongsForGenre(String genre, int limit) {
        Map<String, List<SongDto>> fallbackGenreSamples = new HashMap<>();

        // === CORRECCIÓN AQUÍ: Asegúrate de que todos los constructores de SongDto tienen 6 argumentos ===
        // ID (null), nombre, artista, spotifyUrl, previewUrl, genreHint
        fallbackGenreSamples.put("Pop", Arrays.asList(
            new SongDto(null, "Blinding Lights", "The Weeknd", "https://open.spotify.com/track/3PjlD4B4o4J4J4J4J4J4J4", "", "Pop"),
            new SongDto(null, "Shape of You", "Ed Sheeran", "https://open.spotify.com/track/7qiZfU4dY1lWllzX7pLGYa", "", "Pop"),
            new SongDto(null, "Uptown Funk", "Mark Ronson ft. Bruno Mars", "https://open.spotify.com/track/32OlwWuMpZ6b0aN2RZOeMS", "", "Pop")
        ));
        fallbackGenreSamples.put("Rock", Arrays.asList(
            new SongDto(null, "Bohemian Rhapsody", "Queen", "https://open.spotify.com/track/7tFiyTwD0FpgFfppXclCzo", "", "Rock"),
            new SongDto(null, "Stairway to Heaven", "Led Zeppelin", "https://open.spotify.com/track/5Pz0y30Jp4J4J4J4J4J4J4", "", "Rock"),
            new SongDto(null, "Smells Like Teen Spirit", "Nirvana", "https://open.spotify.com/track/4jC5S555555555555555555", "", "Rock")
        ));
        // Añade aquí el resto de tus géneros de fallback con SongDto de 6 argumentos
        fallbackGenreSamples.put("Indie", Arrays.asList(
            new SongDto(null, "Riptide", "Vance Joy", "https://open.spotify.com/track/7yq4Qj7KGxetoBWPbc5nfP", "", "Indie"),
            new SongDto(null, "Sweater Weather", "The Neighbourhood", "https://open.spotify.com/track/2QjF0D8UkXyswXJ9txtpY2", "", "Indie")
        ));
        fallbackGenreSamples.put("Dance", Arrays.asList(
            new SongDto(null, "Titanium", "David Guetta ft. Sia", "https://open.spotify.com/track/2fE8FqXQd8X8X8X8X8X8X8", "", "Dance"),
            new SongDto(null, "Levels", "Avicii", "https://open.spotify.com/track/5Pz0y30Jp4J4J4J4J4J4J4", "", "Dance")
        ));
        fallbackGenreSamples.put("Blues", Arrays.asList(
            new SongDto(null, "The Thrill Is Gone", "B.B. King", "https://open.spotify.com/track/4tQy6p5X0Q5X0Q5X0Q5X0Q", "", "Blues")
        ));
        fallbackGenreSamples.put("Metal", Arrays.asList(
            new SongDto(null, "Master of Puppets", "Metallica", "https://open.spotify.com/track/2tQy6p5X0Q5X0Q5X0Q5X0Q", "", "Metal")
        ));
        fallbackGenreSamples.put("Ambient", Arrays.asList(
            new SongDto(null, "Weightless", "Marconi Union", "https://open.spotify.com/track/5Pz0y30Jp4J4J4J4J4J4J4", "", "Ambient")
        ));
        fallbackGenreSamples.put("R&B", Arrays.asList(
            new SongDto(null, "Crazy in Love", "Beyoncé ft. Jay-Z", "https://open.spotify.com/track/2tQy6p5X0Q5X0Q5X0Q5X0Q", "", "R&B")
        ));
        fallbackGenreSamples.put("Sad Pop", Arrays.asList(
            new SongDto(null, "Someone You Loved", "Lewis Capaldi", "https://open.spotify.com/track/4tQy6p5X0Q5X0Q5X0Q5X0Q", "", "Sad Pop")
        ));
        fallbackGenreSamples.put("Electronic", Arrays.asList(
            new SongDto(null, "Strobe", "deadmau5", "https://open.spotify.com/track/5Pz0y30Jp4J4J4J4J4J4J4", "", "Electronic")
        ));
        fallbackGenreSamples.put("Lo-Fi", Arrays.asList(
            new SongDto(null, "Coffee Shop", "Lo-Fi Beats", "https://open.spotify.com/track/2tQy6p5X0Q5X0Q5X0Q5X0Q", "", "Lo-Fi")
        ));
        fallbackGenreSamples.put("Reggae", Arrays.asList(
            new SongDto(null, "No Woman, No Cry", "Bob Marley & The Wailers", "https://open.spotify.com/track/4tQy6p5X0Q5X0Q5X0Q5X0Q", "", "Reggae")
        ));
        fallbackGenreSamples.put("Gothic Metal", Arrays.asList(
            new SongDto(null, "Nemo", "Nightwish", "https://open.spotify.com/track/5Pz0y30Jp4J4J4J4J4J4J4", "", "Gothic Metal")
        ));
        fallbackGenreSamples.put("EDM", Arrays.asList(
            new SongDto(null, "Animals", "Martin Garrix", "https://open.spotify.com/track/2tQy6p5X0Q5X0Q5X0Q5X0Q", "", "EDM")
        ));
        fallbackGenreSamples.put("New Age", Arrays.asList(
            new SongDto(null, "Orinoco Flow", "Enya", "https://open.spotify.com/track/4tQy6p5X0Q5X0Q5X0Q5X0Q", "", "New Age")
        ));
        fallbackGenreSamples.put("Classic Rock", Arrays.asList(
            new SongDto(null, "Sweet Child O' Mine", "Guns N' Roses", "https://open.spotify.com/track/5Pz0y30Jp4J4J4J4J4J4J4", "", "Classic Rock")
        ));
        fallbackGenreSamples.put("Latin Pop", Arrays.asList(
            new SongDto(null, "Despacito", "Luis Fonsi ft. Daddy Yankee", "https://open.spotify.com/track/2tQy6p5X0Q5X0Q5X0Q5X0Q", "", "Latin Pop")
        ));
        fallbackGenreSamples.put("Classical", Arrays.asList(
            new SongDto(null, "Clair de Lune", "Claude Debussy", "https://open.spotify.com/track/4tQy6p5X0Q5X0Q5X0Q5X0Q", "", "Classical")
        ));
        fallbackGenreSamples.put("Game Soundtrack", Arrays.asList(
            new SongDto(null, "Megalovania", "Toby Fox", "https://open.spotify.com/track/5Pz0y30Jp4J4J4J4J4J4J4", "", "Game Soundtrack")
        ));
        fallbackGenreSamples.put("Acoustic", Arrays.asList(
            new SongDto(null, "I'm Yours", "Jason Mraz", "https://open.spotify.com/track/2tQy6p5X0Q5X0Q5X0Q5X0Q", "", "Acoustic")
        ));
        fallbackGenreSamples.put("Trap", Arrays.asList(
            new SongDto(null, "Harlem Shake", "Baauer", "https://open.spotify.com/track/4tQy6p5X0Q5X0Q5X0Q5X0Q", "", "Trap")
        ));
        fallbackGenreSamples.put("Gospel", Arrays.asList(
            new SongDto(null, "Oh Happy Day", "Edwin Hawkins Singers", "https://open.spotify.com/track/5Pz0y30Jp4J4J4J4J4J4J4", "", "Gospel")
        ));
        fallbackGenreSamples.put("Chillwave", Arrays.asList(
            new SongDto(null, "Feel It All Around", "Washed Out", "https://open.spotify.com/track/2tQy6p5X0Q5X0Q5X0Q5X0Q", "", "Chillwave")
        ));
        fallbackGenreSamples.put("Hard Rock", Arrays.asList(
            new SongDto(null, "Highway to Hell", "AC/DC", "https://open.spotify.com/track/4tQy6p5X0Q5X0Q5X0Q5X0Q", "", "Hard Rock")
        ));
        fallbackGenreSamples.put("Various", Arrays.asList(
            new SongDto(null, "Happy", "Pharrell Williams", "https://open.spotify.com/track/5Pz0y30Jp4J4J4J4J4J4J4", "", "Pop")
        ));
        fallbackGenreSamples.put("Reggaeton", Arrays.asList(
            new SongDto(null, "Gasolina", "Daddy Yankee", "https://open.spotify.com/track/6x6s7f7f7f7f7f7f7f7f7f7f", "", "Reggaeton")
        ));
        fallbackGenreSamples.put("Balada", Arrays.asList(
            new SongDto(null, "Contigo en la distancia", "Christina Aguilera", "https://open.spotify.com/track/62x6s7f7f7f7f7f7f7f7f7f7f", "", "Balada")
        ));
        fallbackGenreSamples.put("Electrónica", Arrays.asList(
            new SongDto(null, "Strobe", "deadmau5", "https://open.spotify.com/track/72x6s7f7f7f7f7f7f7f7f7f7f", "", "Electrónica")
        ));
        fallbackGenreSamples.put("Jazz", Arrays.asList(
            new SongDto(null, "Take Five", "Dave Brubeck Quartet", "https://open.spotify.com/track/82x6s7f7f7f7f7f7f7f7f7f7f", "", "Jazz")
        ));
        fallbackGenreSamples.put("K-Pop", Arrays.asList(
            new SongDto(null, "Dynamite", "BTS", "https://open.spotify.com/track/92x6s7f7f7f7f7f7f7f7f7f7f", "", "K-Pop")
        ));
        fallbackGenreSamples.put("Heavy Metal", Arrays.asList(
            new SongDto(null, "Master of Puppets", "Metallica", "https://open.spotify.com/track/12x6s7f7f7f7f7f7f7f7f7f7f", "", "Heavy Metal")
        ));
        fallbackGenreSamples.put("Country", Arrays.asList(
            new SongDto(null, "Take Me Home, Country Roads", "John Denver", "https://open.spotify.com/track/22x6s7f7f7f7f7f7f7f7f7f7f", "", "Country")
        ));
        fallbackGenreSamples.put("Folk", Arrays.asList(
            new SongDto(null, "Blowin' in the Wind", "Bob Dylan", "https://open.spotify.com/track/32x6s7f7f7f7f7f7f7f7f7f7f", "", "Folk")
        ));
        fallbackGenreSamples.put("Smooth Jazz", Arrays.asList(
            new SongDto(null, "Morning Dance", "Spyro Gyra", "https://open.spotify.com/track/42x6s7f7f7f7f7f7f7f7f7f7f", "", "Smooth Jazz")
        ));
        fallbackGenreSamples.put("Big Band", Arrays.asList(
            new SongDto(null, "In the Mood", "Glenn Miller", "https://open.spotify.com/track/52x6s7f7f7f7f7f7f7f7f7f7f", "", "Big Band")
        ));
        fallbackGenreSamples.put("Soul", Arrays.asList(
            new SongDto(null, "What's Going On", "Marvin Gaye", "https://open.spotify.com/track/62x6s7f7f7f7f7f7f7f7f7f7f", "", "Soul")
        ));
        fallbackGenreSamples.put("Funk", Arrays.asList(
            new SongDto(null, "Super Freak", "Rick James", "https://open.spotify.com/track/72x6s7f7f7f7f7f7f7f7f7f7f", "", "Funk")
        ));
        fallbackGenreSamples.put("Disco", Arrays.asList(
            new SongDto(null, "Stayin' Alive", "Bee Gees", "https://open.spotify.com/track/82x6s7f7f7f7f7f7f7f7f7f7f", "", "Disco")
        ));
        fallbackGenreSamples.put("Punk", Arrays.asList(
            new SongDto(null, "Blitzkrieg Bop", "Ramones", "https://open.spotify.com/track/92x6s7f7f7f7f7f7f7f7f7f7f", "", "Punk")
        ));
        fallbackGenreSamples.put("Grunge", Arrays.asList(
            new SongDto(null, "Smells Like Teen Spirit", "Nirvana", "https://open.spotify.com/track/13x6s7f7f7f7f7f7f7f7f7f7f", "", "Grunge")
        ));
        fallbackGenreSamples.put("Metalcore", Arrays.asList(
            new SongDto(null, "The End of Heartache", "Killswitch Engage", "https://open.spotify.com/track/23x6s7f7f7f7f7f7f7f7f7f7f", "", "Metalcore")
        ));
        fallbackGenreSamples.put("Death Metal", Arrays.asList(
            new SongDto(null, "Hammer Smashed Face", "Cannibal Corpse", "https://open.spotify.com/track/33x6s7f7f7f7f7f7f7f7f7f7f", "", "Death Metal")
        ));
        fallbackGenreSamples.put("Black Metal", Arrays.asList(
            new SongDto(null, "Freezing Moon", "Mayhem", "https://open.spotify.com/track/43x6s7f7f7f7f7f7f7f7f7f7f", "", "Black Metal")
        ));
        fallbackGenreSamples.put("Symphonic Metal", Arrays.asList(
            new SongDto(null, "Nemo", "Nightwish", "https://open.spotify.com/track/53x6s7f7f7f7f7f7f7f7f7f7f", "", "Symphonic Metal")
        ));
        fallbackGenreSamples.put("Progressive Rock", Arrays.asList(
            new SongDto(null, "Comfortably Numb", "Pink Floyd", "https://open.spotify.com/track/63x6s7f7f7f7f7f7f7f7f7f7f", "", "Progressive Rock")
        ));
        fallbackGenreSamples.put("Psychedelic Rock", Arrays.asList(
            new SongDto(null, "Light My Fire", "The Doors", "https://open.spotify.com/track/73x6s7f7f7f7f7f7f7f7f7f7f", "", "Psychedelic Rock")
        ));
        fallbackGenreSamples.put("Lo-fi", Arrays.asList(
            new SongDto(null, "Lo-fi Study Beats", "Lofi Girl", "https://open.spotify.com/track/83x6s7f7f7f7f7f7f7f7f7f7f", "", "Lo-fi")
        ));
        fallbackGenreSamples.put("Chillwave", Arrays.asList(
            new SongDto(null, "Feel It All Around", "Washed Out", "https://open.spotify.com/track/93x6s7f7f7f7f7f7f7f7f7f7f", "", "Chillwave")
        ));
        fallbackGenreSamples.put("Synthwave", Arrays.asList(
            new SongDto(null, "Nightcall", "Kavinsky", "https://open.spotify.com/track/14x6s7f7f7f7f7f7f7f7f7f7f", "", "Synthwave")
        ));
        fallbackGenreSamples.put("Trance", Arrays.asList(
            new SongDto(null, "Adagio for Strings", "Tiësto", "https://open.spotify.com/track/24x6s7f7f7f7f7f7f7f7f7f7f", "", "Trance")
        ));
        fallbackGenreSamples.put("House", Arrays.asList(
            new SongDto(null, "One More Time", "Daft Punk", "https://open.spotify.com/track/34x6s7f7f7f7f7f7f7f7f7f7f", "", "House")
        ));
        fallbackGenreSamples.put("Techno", Arrays.asList(
            new SongDto(null, "Insomnia", "Faithless", "https://open.spotify.com/track/44x6s7f7f7f7f7f7f7f7f7f7f", "", "Techno")
        ));
        fallbackGenreSamples.put("Dubstep", Arrays.asList(
            new SongDto(null, "Scary Monsters and Nice Sprites", "Skrillex", "https://open.spotify.com/track/54x6s7f7f7f7f7f7f7f7f7f7f", "", "Dubstep")
        ));
        fallbackGenreSamples.put("Drum & Bass", Arrays.asList(
            new SongDto(null, "Inner City Life", "Goldie", "https://open.spotify.com/track/64x6s7f7f7f7f7f7f7f7f7f7f", "", "Drum & Bass")
        ));
        fallbackGenreSamples.put("Reggae", Arrays.asList(
            new SongDto(null, "One Love", "Bob Marley & The Wailers", "https://open.spotify.com/track/74x6s7f7f7f7f7f7f7f7f7f7f", "", "Reggae")
        ));
        fallbackGenreSamples.put("Salsa", Arrays.asList(
            new SongDto(null, "La Vida Es Un Carnaval", "Celia Cruz", "https://open.spotify.com/track/84x6s7f7f7f7f7f7f7f7f7f7f", "", "Salsa")
        ));
        fallbackGenreSamples.put("Flamenco", Arrays.asList(
            new SongDto(null, "Entre Dos Aguas", "Paco de Lucía", "https://open.spotify.com/track/94x6s7f7f7f7f7f7f7f7f7f7f", "", "Flamenco")
        ));
        fallbackGenreSamples.put("Gospel", Arrays.asList(
            new SongDto(null, "Oh Happy Day", "Edwin Hawkins Singers", "https://open.spotify.com/track/15x6s7f7f7f7f7f7f7f7f7f7f", "", "Gospel")
        ));
        fallbackGenreSamples.put("Opera", Arrays.asList(
            new SongDto(null, "Nessun Dorma", "Giacomo Puccini", "https://open.spotify.com/track/25x6s7f7f7f7f7f7f7f7f7f7f", "", "Opera")
        ));
        fallbackGenreSamples.put("World Music", Arrays.asList(
            new SongDto(null, "Pata Pata", "Miriam Makeba", "https://open.spotify.com/track/35x6s7f7f7f7f7f7f7f7f7f7f", "", "World Music")
        ));
        fallbackGenreSamples.put("Bollywood", Arrays.asList(
            new SongDto(null, "Jai Ho!", "A.R. Rahman", "https://open.spotify.com/track/45x6s7f7f7f7f7f7f7f7f7f7f", "", "Bollywood")
        ));
        fallbackGenreSamples.put("Anime OST", Arrays.asList(
            new SongDto(null, "Gurenge", "LiSA", "https://open.spotify.com/track/55x6s7f7f7f7f7f7f7f7f7f7f", "", "Anime OST")
        ));
        fallbackGenreSamples.put("Video Game OST", Arrays.asList(
            new SongDto(null, "One-Winged Angel", "Final Fantasy VII", "https://open.spotify.com/track/65x6s7f7f7f7f7f7f7f7f7f7f", "", "Video Game OST")
        ));
        fallbackGenreSamples.put("Film Score", Arrays.asList(
            new SongDto(null, "Hedwig's Theme", "Harry Potter", "https://open.spotify.com/track/75x6s7f7f7f7f7f7f7f7f7f7f", "", "Film Score")
        ));
        fallbackGenreSamples.put("Childrens Music", Arrays.asList(
            new SongDto(null, "Baby Shark", "Pinkfong", "https://open.spotify.com/track/85x6s7f7f7f7f7f7f7f7f7f7f", "", "Childrens Music")
        ));
        fallbackGenreSamples.put("Holiday Music", Arrays.asList(
            new SongDto(null, "All I Want for Christmas Is You", "Mariah Carey", "https://open.spotify.com/track/95x6s7f7f7f7f7f7f7f7f7f7f", "", "Holiday Music")
        ));
        fallbackGenreSamples.put("Spoken Word", Arrays.asList(
            new SongDto(null, "The Raven", "Edgar Allan Poe", "https://open.spotify.com/track/16x6s7f7f7f7f7f7f7f7f7f7f", "", "Spoken Word")
        ));
        fallbackGenreSamples.put("Comedy", Arrays.asList(
            new SongDto(null, "Always Look on the Bright Side of Life", "Monty Python", "https://open.spotify.com/track/26x6s7f7f7f7f7f7f7f7f7f7f", "", "Comedy")
        ));
        fallbackGenreSamples.put("Trap", Arrays.asList(
            new SongDto(null, "Trap Queen", "Fetty Wap", "https://open.spotify.com/track/36x6s7f7f7f7f7f7f7f7f7f7f", "", "Trap")
        ));
        fallbackGenreSamples.put("Drill", Arrays.asList(
            new SongDto(null, "I Don't Like", "Chief Keef", "https://open.spotify.com/track/46x6s7f7f7f7f7f7f7f7f7f7f", "", "Drill")
        ));
        fallbackGenreSamples.put("Grime", Arrays.asList(
            new SongDto(null, "Pow! (Forward)", "Lethal Bizzle", "https://open.spotify.com/track/56x6s7f7f7f7f7f7f7f7f7f7f", "", "Grime")
        ));
        fallbackGenreSamples.put("K-R&B", Arrays.asList(
            new SongDto(null, "Crush", "Crush", "https://open.spotify.com/track/66x6s7f7f7f7f7f7f7f7f7f7f", "", "K-R&B")
        ));
        fallbackGenreSamples.put("J-Rock", Arrays.asList(
            new SongDto(null, "Guren no Yumiya", "Linked Horizon", "https://open.spotify.com/track/76x6s7f7f7f7f7f7f7f7f7f7f", "", "J-Rock")
        ));
        fallbackGenreSamples.put("Pop Punk", Arrays.asList(
            new SongDto(null, "What's My Age Again?", "Blink-182", "https://open.spotify.com/track/86x6s7f7f7f7f7f7f7f7f7f7f", "", "Pop Punk")
        ));
        fallbackGenreSamples.put("Emo", Arrays.asList(
            new SongDto(null, "Welcome to the Black Parade", "My Chemical Romance", "https://open.spotify.com/track/96x6s7f7f7f7f7f7f7f7f7f7f", "", "Emo")
        ));
        fallbackGenreSamples.put("Folk Punk", Arrays.asList(
            new SongDto(null, "A Toast to the Future Kids!", "Days N' Daze", "https://open.spotify.com/track/17x6s7f7f7f7f7f7f7f7f7f7f", "", "Folk Punk")
        ));
        fallbackGenreSamples.put("Indie Pop", Arrays.asList(
            new SongDto(null, "Pumped Up Kicks", "Foster the People", "https://open.spotify.com/track/27x6s7f7f7f7f7f7f7f7f7f7f", "", "Indie Pop")
        ));
        fallbackGenreSamples.put("Dream Pop", Arrays.asList(
            new SongDto(null, "Space Song", "Beach House", "https://open.spotify.com/track/37x6s7f7f7f7f7f7f7f7f7f7f", "", "Dream Pop")
        ));
        fallbackGenreSamples.put("Neoclassical", Arrays.asList(
            new SongDto(null, "Nuvole Bianche", "Ludovico Einaudi", "https://open.spotify.com/track/47x6s7f7f7f7f7f7f7f7f7f7f", "", "Neoclassical")
        ));
        fallbackGenreSamples.put("Choral", Arrays.asList(
            new SongDto(null, "Hallelujah Chorus", "Handel's Messiah", "https://open.spotify.com/track/57x6s7f7f7f7f7f7f7f7f7f7f", "", "Choral")
        ));
        fallbackGenreSamples.put("New Age", Arrays.asList(
            new SongDto(null, "Orinoco Flow", "Enya", "https://open.spotify.com/track/67x6s7f7f7f7f7f7f7f7f7f7f", "", "New Age")
        ));
        fallbackGenreSamples.put("Spa Music", Arrays.asList(
            new SongDto(null, "Weightless", "Marconi Union", "https://open.spotify.com/track/77x6s7f7f7f7f7f7f7f7f7f7f", "", "Spa Music")
        ));
        fallbackGenreSamples.put("Nature Sounds", Arrays.asList(
            new SongDto(null, "Rain Sounds for Sleep", "Nature Sounds", "https://open.spotify.com/track/87x6s7f7f7f7f7f7f7f7f7f7f", "", "Nature Sounds")
        ));
        fallbackGenreSamples.put("ASMR", Arrays.asList(
            new SongDto(null, "ASMR Tapping Sounds", "ASMR Darling", "https://open.spotify.com/track/97x6s7f7f7f7f7f7f7f7f7f7f", "", "ASMR")
        ));
        fallbackGenreSamples.put("Educational Music", Arrays.asList(
            new SongDto(null, "The Alphabet Song", "Traditional", "https://open.spotify.com/track/18x6s7f7f7f7f7f7f7f7f7f7f", "", "Educational Music")
        ));
        fallbackGenreSamples.put("Novelty Songs", Arrays.asList(
            new SongDto(null, "The Hamsterdance Song", "Hampton the Hamster", "https://open.spotify.com/track/28x6s7f7f7f7f7f7f7f7f7f7f", "", "Novelty Songs")
        ));
        fallbackGenreSamples.put("Political Hip Hop", Arrays.asList(
            new SongDto(null, "Fight the Power", "Public Enemy", "https://open.spotify.com/track/38x6s7f7f7f7f7f7f7f7f7f7f", "", "Political Hip Hop")
        ));
        fallbackGenreSamples.put("Power Metal", Arrays.asList(
            new SongDto(null, "The Bard's Song (In the Forest)", "Blind Guardian", "https://open.spotify.com/track/48x6s7f7f7f7f7f7f7f7f7f7f", "", "Power Metal")
        ));
        fallbackGenreSamples.put("Viking Metal", Arrays.asList(
            new SongDto(null, "Twilight of the Thunder God", "Amon Amarth", "https://open.spotify.com/track/58x6s7f7f7f7f7f7f7f7f7f7f", "", "Viking Metal")
        ));
        fallbackGenreSamples.put("Pirate Metal", Arrays.asList(
            new SongDto(null, "Keelhauled", "Alestorm", "https://open.spotify.com/track/68x6s7f7f7f7f7f7f7f7f7f7f", "", "Pirate Metal")
        ));
        fallbackGenreSamples.put("Medieval Metal", Arrays.asList(
            new SongDto(null, "In Taberna", "Corvus Corax", "https://open.spotify.com/track/78x6s7f7f7f7f7f7f7f7f7f7f", "", "Medieval Metal")
        ));
        fallbackGenreSamples.put("Pagan Metal", Arrays.asList(
            new SongDto(null, "Korpiklaani - Vodka", "Korpiklaani", "https://open.spotify.com/track/88x6s7f7f7f7f7f7f7f7f7f7f", "", "Pagan Metal")
        ));
        fallbackGenreSamples.put("Blackened Thrash Metal", Arrays.asList(
            new SongDto(null, "Total Destruction", "Desaster", "https://open.spotify.com/track/98x6s7f7f7f7f7f7f7f7f7f7f", "", "Blackened Thrash Metal")
        ));
        fallbackGenreSamples.put("Technical Death Metal", Arrays.asList(
            new SongDto(null, "Crystal Mountain", "Death", "https://open.spotify.com/track/19x6s7f7f7f7f7f7f7f7f7f7f", "", "Technical Death Metal")
        ));
        fallbackGenreSamples.put("Brutal Death Metal", Arrays.asList(
            new SongDto(null, "Hammer Smashed Face", "Cannibal Corpse", "https://open.spotify.com/track/29x6s7f7f7f7f7f7f7f7f7f7f", "", "Brutal Death Metal")
        ));
        fallbackGenreSamples.put("Slam Death Metal", Arrays.asList(
            new SongDto(null, "Hammer Smashed Face", "Cannibal Corpse", "https://open.spotify.com/track/39x6s7f7f7f7f7f7f7f7f7f7f", "", "Slam Death Metal")
        ));
        fallbackGenreSamples.put("Progressive Death Metal", Arrays.asList(
            new SongDto(null, "Crystal Mountain", "Death", "https://open.spotify.com/track/49x6s7f7f7f7f7f7f7f7f7f7f", "", "Progressive Death Metal")
        ));
        fallbackGenreSamples.put("Melodic Death Metal", Arrays.asList(
            new SongDto(null, "Blinded by Fear", "At the Gates", "https://open.spotify.com/track/59x6s7f7f7f7f7f7f7f7f7f7f", "", "Melodic Death Metal")
        ));
        fallbackGenreSamples.put("Funeral Doom Metal", Arrays.asList(
            new SongDto(null, "The Dreadful Hours", "My Dying Bride", "https://open.spotify.com/track/69x6s7f7f7f7f7f7f7f7f7f7f", "", "Funeral Doom Metal")
        ));
        fallbackGenreSamples.put("Atmospheric Black Metal", Arrays.asList(
            new SongDto(null, "Lost Wisdom", "Burzum", "https://open.spotify.com/track/79x6s7f7f7f7f7f7f7f7f7f7f", "", "Atmospheric Black Metal")
        ));
        fallbackGenreSamples.put("Depressive Suicidal Black Metal (DSBM)", Arrays.asList(
            new SongDto(null, "Suicide Is Painless", "Shining", "https://open.spotify.com/track/89x6s7f7f7f7f7f7f7f7f7f7f", "", "Depressive Suicidal Black Metal (DSBM)")
        ));
        fallbackGenreSamples.put("Post-Black Metal", Arrays.asList(
            new SongDto(null, "Sunbather", "Deafheaven", "https://open.spotify.com/track/99x6s7f7f7f7f7f7f7f7f7f7f", "", "Post-Black Metal")
        ));
        fallbackGenreSamples.put("Raw Black Metal", Arrays.asList(
            new SongDto(null, "Transilvanian Hunger", "Darkthrone", "https://open.spotify.com/track/10x6s7f7f7f7f7f7f7f7f7f7f", "", "Raw Black Metal")
        ));
        fallbackGenreSamples.put("Blackgaze", Arrays.asList(
            new SongDto(null, "Sunbather", "Deafheaven", "https://open.spotify.com/track/20x6s7f7f7f7f7f7f7f7f7f7f", "", "Blackgaze")
        ));
        fallbackGenreSamples.put("Industrial Black Metal", Arrays.asList(
            new SongDto(null, "The Grand Declaration of War", "Mayhem", "https://open.spotify.com/track/30x6s7f7f7f7f7f7f7f7f7f7f", "", "Industrial Black Metal")
        ));
        fallbackGenreSamples.put("Folk Black Metal", Arrays.asList(
            new SongDto(null, "Korpiklaani - Vodka", "Korpiklaani", "https://open.spotify.com/track/40x6s7f7f7f7f7f7f7f7f7f7f", "", "Folk Black Metal")
        ));
        fallbackGenreSamples.put("War Metal", Arrays.asList(
            new SongDto(null, "F.O.A.D.", "Goatwhore", "https://open.spotify.com/track/50x6s7f7f7f7f7f7f7f7f7f7f", "", "War Metal")
        ));
        fallbackGenreSamples.put("Powerviolence", Arrays.asList(
            new SongDto(null, "You Suffer", "Napalm Death", "https://open.spotify.com/track/60x6s7f7f7f7f7f7f7f7f7f7f", "", "Powerviolence")
        ));
        fallbackGenreSamples.put("No Wave", Arrays.asList(
            new SongDto(null, "Contort Yourself", "James Chance and the Contortions", "https://open.spotify.com/track/70x6s7f7f7f7f7f7f7f7f7f7f", "", "No Wave")
        ));
        fallbackGenreSamples.put("Free Improvisation", Arrays.asList(
            new SongDto(null, "Machine Gun", "Peter Brötzmann Octet", "https://open.spotify.com/track/80x6s7f7f7f7f7f7f7f7f7f7f", "", "Free Improvisation")
        ));
        fallbackGenreSamples.put("Experimental Rock", Arrays.asList(
            new SongDto(null, "I Am the Walrus", "The Beatles", "https://open.spotify.com/track/90x6s7f7f7f7f7f7f7f7f7f7f", "", "Experimental Rock")
        ));
        fallbackGenreSamples.put("Avant-garde Metal", Arrays.asList(
            new SongDto(null, "The Great Southern Trendkill", "Pantera", "https://open.spotify.com/track/11x6s7f7f7f7f7f7f7f7f7f7f", "", "Avant-garde Metal")
        ));
        fallbackGenreSamples.put("Drone Metal", Arrays.asList(
            new SongDto(null, "Monoliths & Dimensions", "Sunn O)))", "https://open.spotify.com/track/21x6s7f7f7f7f7f7f7f7f7f7f", "", "Drone Metal")
        ));
        fallbackGenreSamples.put("Sludgecore", Arrays.asList(
            new SongDto(null, "Take as Needed for Pain", "Eyehategod", "https://open.spotify.com/track/31x6s7f7f7f7f7f7f7f7f7f7f", "", "Sludgecore")
        ));
        fallbackGenreSamples.put("Post-Metal", Arrays.asList(
            new SongDto(null, "Panopticon", "Isis", "https://open.spotify.com/track/41x6s7f7f7f7f7f7f7f7f7f7f", "", "Post-Metal")
        ));
        fallbackGenreSamples.put("Stoner Doom", Arrays.asList(
            new SongDto(null, "Dopesmoker", "Sleep", "https://open.spotify.com/track/51x6s7f7f7f7f7f7f7f7f7f7f", "", "Stoner Doom")
        ));
        fallbackGenreSamples.put("Psychedelic Doom", Arrays.asList(
            new SongDto(null, "Dopesmoker", "Sleep", "https://open.spotify.com/track/61x6s7f7f7f7f7f7f7f7f7f7f", "", "Psychedelic Doom")
        ));
        fallbackGenreSamples.put("Traditional Doom Metal", Arrays.asList(
            new SongDto(null, "Black Sabbath", "Black Sabbath", "https://open.spotify.com/track/71x6s7f7f7f7f7f7f7f7f7f7f", "", "Traditional Doom Metal")
        ));
        fallbackGenreSamples.put("Epic Doom Metal", Arrays.asList(
            new SongDto(null, "Solitude", "Candlemass", "https://open.spotify.com/track/81x6s7f7f7f7f7f7f7f7f7f7f", "", "Epic Doom Metal")
        ));
        fallbackGenreSamples.put("Folk Doom Metal", Arrays.asList(
            new SongDto(null, "The Dreadful Hours", "My Dying Bride", "https://open.spotify.com/track/91x6s7f7f7f7f7f7f7f7f7f7f", "", "Folk Doom Metal")
        ));
        fallbackGenreSamples.put("Progressive Doom Metal", Arrays.asList(
            new SongDto(null, "Blackwater Park", "Opeth", "https://open.spotify.com/track/12x6s7f7f7f7f7f7f7f7f7f7f", "", "Progressive Doom Metal")
        ));
        fallbackGenreSamples.put("Sludge Doom Metal", Arrays.asList(
            new SongDto(null, "Take as Needed for Pain", "Eyehategod", "https://open.spotify.com/track/22x6s7f7f7f7f7f7f7f7f7f7f", "", "Sludge Doom Metal")
        ));
        fallbackGenreSamples.put("Funeral Doom Metal", Arrays.asList(
            new SongDto(null, "The Dreadful Hours", "My Dying Bride", "https://open.spotify.com/track/32x6s7f7f7f7f7f7f7f7f7f7f", "", "Funeral Doom Metal")
        ));
        // ====================================================================================================

        List<SongDto> fallbackSongs = fallbackGenreSamples.getOrDefault(genre,
            Collections.singletonList(new SongDto(null, "No hay recomendaciones", "N/A", "", "", "Desconocido"))); // Corregido constructor

        return fallbackSongs.stream().limit(limit).collect(Collectors.toList());
    }
}