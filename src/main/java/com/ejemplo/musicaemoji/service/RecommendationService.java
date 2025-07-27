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
    @SuppressWarnings("unchecked") // Suprimir la advertencia de "unchecked cast" para Mono.zip
    public Mono<RecommendationResponse> getRecommendationsByEmojis(String emojis) {
        if (emojis == null || emojis.trim().isEmpty()) {
            // Especificar tipos genéricos para emptyList y emptySet
            return Mono.just(new RecommendationResponse(Collections.<SongDto>emptyList(), Collections.<String>emptySet()));
        }

        Set<String> recommendedGenres = new HashSet<>();
        // === CORRECCIÓN CLAVE AQUÍ: Usar codePoints() para manejar emojis multi-codepoint ===
        emojis.codePoints().mapToObj(Character::toString).forEach(emojiString -> {
            emojiMoodRepository.findByEmoji(emojiString).ifPresent(emojiMood ->
                recommendedGenres.add(emojiMood.getGenreHint())
            );
        });

        // Lógica de fallback específica si no se encontraron géneros directamente en la base de datos
        if (recommendedGenres.isEmpty()) {
            emojis.codePoints().mapToObj(Character::toString).forEach(emojiString -> {
                if ("🤷‍♀️".equals(emojiString)) {
                    recommendedGenres.add("Indie");
                } else if ("🎉".equals(emojiString)) {
                    recommendedGenres.add("Pop");
                    recommendedGenres.add("Dance");
                }
                // Puedes añadir más lógica de fallback aquí para otros emojis si es necesario
            });
        }

        // Fallback general: Si después de todas las búsquedas, aún no se ha recomendado ningún género
        if (recommendedGenres.isEmpty() && !emojis.isEmpty()) {
            recommendedGenres.add("Indie"); // Género por defecto si no se encuentra nada
        }

        // Filtra cualquier posible cadena vacía y asegura que no haya duplicados
        Set<String> finalRecommendedGenres = recommendedGenres.stream()
                                .filter(genre -> !genre.isEmpty())
                                .collect(Collectors.toSet());

        System.out.println("DEBUG RecommendationService: Géneros finales recomendados antes de Spotify: " + finalRecommendedGenres);

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
        // Añade aquí el resto de tus géneros de fallback con SongDto de 5 argumentos
        fallbackGenreSamples.put("Indie", Arrays.asList(
            new SongDto("Riptide", "Vance Joy", "https://open.spotify.com/track/7yq4Qj7KGxetoBWPbc5nfP", "https://p.scdn.co/mp3-preview/a9d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Indie"),
            new SongDto("Sweater Weather", "The Neighbourhood", "https://open.spotify.com/track/2QjF0D8UkXyswXJ9txtpY2", "https://p.scdn.co/mp3-preview/b9d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Indie")
        ));
        fallbackGenreSamples.put("Dance", Arrays.asList(
            new SongDto("Titanium", "David Guetta ft. Sia", "https://open.spotify.com/track/2fE8FqXQd8X8X8X8X8X8X8", "https://p.scdn.co/mp3-preview/c9d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Dance"),
            new SongDto("Levels", "Avicii", "https://open.spotify.com/track/5Pz0y30Jp4J4J4J4J4J4J4", "https://p.scdn.co/mp3-preview/d9d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Dance")
        ));
        fallbackGenreSamples.put("Blues", Arrays.asList(
            new SongDto("The Thrill Is Gone", "B.B. King", "https://open.spotify.com/track/4tQy6p5X0Q5X0Q5X0Q5X0Q", "https://p.scdn.co/mp3-preview/e9d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Blues")
        ));
        fallbackGenreSamples.put("Metal", Arrays.asList(
            new SongDto("Master of Puppets", "Metallica", "https://open.spotify.com/track/2tQy6p5X0Q5X0Q5X0Q5X0Q", "https://p.scdn.co/mp3-preview/f9d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Metal")
        ));
        fallbackGenreSamples.put("Ambient", Arrays.asList(
            new SongDto("Weightless", "Marconi Union", "https://open.spotify.com/track/5Pz0y30Jp4J4J4J4J4J4J4", "https://p.scdn.co/mp3-preview/10d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Ambient")
        ));
        fallbackGenreSamples.put("R&B", Arrays.asList(
            new SongDto("Crazy in Love", "Beyoncé ft. Jay-Z", "https://open.spotify.com/track/2tQy6p5X0Q5X0Q5X0Q5X0Q", "https://p.scdn.co/mp3-preview/20d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "R&B")
        ));
        fallbackGenreSamples.put("Sad Pop", Arrays.asList(
            new SongDto("Someone You Loved", "Lewis Capaldi", "https://open.spotify.com/track/4tQy6p5X0Q5X0Q5X0Q5X0Q", "https://p.scdn.co/mp3-preview/30d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Sad Pop")
        ));
        fallbackGenreSamples.put("Electronic", Arrays.asList(
            new SongDto("Strobe", "deadmau5", "https://open.spotify.com/track/5Pz0y30Jp4J4J4J4J4J4J4", "https://p.scdn.co/mp3-preview/40d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Electronic")
        ));
        fallbackGenreSamples.put("Lo-Fi", Arrays.asList(
            new SongDto("Coffee Shop", "Lo-Fi Beats", "https://open.spotify.com/track/2tQy6p5X0Q5X0Q5X0Q5X0Q", "https://p.scdn.co/mp3-preview/50d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Lo-Fi")
        ));
        fallbackGenreSamples.put("Reggae", Arrays.asList(
            new SongDto("No Woman, No Cry", "Bob Marley & The Wailers", "https://open.spotify.com/track/4tQy6p5X0Q5X0Q5X0Q5X0Q", "https://p.scdn.co/mp3-preview/60d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Reggae")
        ));
        fallbackGenreSamples.put("Gothic Metal", Arrays.asList(
            new SongDto("Nemo", "Nightwish", "https://open.spotify.com/track/5Pz0y30Jp4J4J4J4J4J4J4", "https://p.scdn.co/mp3-preview/70d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Gothic Metal")
        ));
        fallbackGenreSamples.put("EDM", Arrays.asList(
            new SongDto("Animals", "Martin Garrix", "https://open.spotify.com/track/2tQy6p5X0Q5X0Q5X0Q5X0Q", "https://p.scdn.co/mp3-preview/80d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "EDM")
        ));
        fallbackGenreSamples.put("New Age", Arrays.asList(
            new SongDto("Orinoco Flow", "Enya", "https://open.spotify.com/track/4tQy6p5X0Q5X0Q5X0Q5X0Q", "https://p.scdn.co/mp3-preview/90d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "New Age")
        ));
        fallbackGenreSamples.put("Classic Rock", Arrays.asList(
            new SongDto("Sweet Child O' Mine", "Guns N' Roses", "https://open.spotify.com/track/5Pz0y30Jp4J4J4J4J4J4J4", "https://p.scdn.co/mp3-preview/a0d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Classic Rock")
        ));
        fallbackGenreSamples.put("Latin Pop", Arrays.asList(
            new SongDto("Despacito", "Luis Fonsi ft. Daddy Yankee", "https://open.spotify.com/track/2tQy6p5X0Q5X0Q5X0Q5X0Q", "https://p.scdn.co/mp3-preview/b0d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Latin Pop")
        ));
        fallbackGenreSamples.put("Classical", Arrays.asList(
            new SongDto("Clair de Lune", "Claude Debussy", "https://open.spotify.com/track/4tQy6p5X0Q5X0Q5X0Q5X0Q", "https://p.scdn.co/mp3-preview/c0d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Classical")
        ));
        fallbackGenreSamples.put("Game Soundtrack", Arrays.asList(
            new SongDto("Megalovania", "Toby Fox", "https://open.spotify.com/track/5Pz0y30Jp4J4J4J4J4J4J4", "https://p.scdn.co/mp3-preview/d0d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Game Soundtrack")
        ));
        fallbackGenreSamples.put("Acoustic", Arrays.asList(
            new SongDto("I'm Yours", "Jason Mraz", "https://open.spotify.com/track/2tQy6p5X0Q5X0Q5X0Q5X0Q", "https://p.scdn.co/mp3-preview/e0d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Acoustic")
        ));
        fallbackGenreSamples.put("Trap", Arrays.asList(
            new SongDto("Harlem Shake", "Baauer", "https://open.spotify.com/track/4tQy6p5X0Q5X0Q5X0Q5X0Q", "https://p.scdn.co/mp3-preview/f0d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Trap")
        ));
        fallbackGenreSamples.put("Gospel", Arrays.asList(
            new SongDto("Oh Happy Day", "Edwin Hawkins Singers", "https://open.spotify.com/track/5Pz0y30Jp4J4J4J4J4J4J4", "https://p.scdn.co/mp3-preview/11d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Gospel")
        ));
        fallbackGenreSamples.put("Chillwave", Arrays.asList(
            new SongDto("Feel It All Around", "Washed Out", "https://open.spotify.com/track/2tQy6p5X0Q5X0Q5X0Q5X0Q", "https://p.scdn.co/mp3-preview/21d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Chillwave")
        ));
        fallbackGenreSamples.put("Hard Rock", Arrays.asList(
            new SongDto("Highway to Hell", "AC/DC", "https://open.spotify.com/track/4tQy6p5X0Q5X0Q5X0Q5X0Q", "https://p.scdn.co/mp3-preview/31d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Hard Rock")
        ));
        fallbackGenreSamples.put("Various", Arrays.asList(
            new SongDto("Happy", "Pharrell Williams", "https://open.spotify.com/track/5Pz0y30Jp4J4J4J4J4J4J4", "https://p.scdn.co/mp3-preview/41d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Pop") // Ejemplo de canción variada
        ));
        fallbackGenreSamples.put("Reggaeton", Arrays.asList(
            new SongDto("Gasolina", "Daddy Yankee", "https://open.spotify.com/track/6x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/51d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Reggaeton")
        ));
        fallbackGenreSamples.put("Balada", Arrays.asList(
            new SongDto("Contigo en la distancia", "Christina Aguilera", "https://open.spotify.com/track/62x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/61d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Balada")
        ));
        fallbackGenreSamples.put("Electrónica", Arrays.asList(
            new SongDto("Strobe", "deadmau5", "https://open.spotify.com/track/72x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/71d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Electrónica")
        ));
        fallbackGenreSamples.put("Jazz", Arrays.asList(
            new SongDto("Take Five", "Dave Brubeck Quartet", "https://open.spotify.com/track/82x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/81d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Jazz")
        ));
        fallbackGenreSamples.put("K-Pop", Arrays.asList(
            new SongDto("Dynamite", "BTS", "https://open.spotify.com/track/92x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/91d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "K-Pop")
        ));
        fallbackGenreSamples.put("Heavy Metal", Arrays.asList(
            new SongDto("Master of Puppets", "Metallica", "https://open.spotify.com/track/12x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/12d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Heavy Metal")
        ));
        fallbackGenreSamples.put("Country", Arrays.asList(
            new SongDto("Take Me Home, Country Roads", "John Denver", "https://open.spotify.com/track/22x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/22d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Country")
        ));
        fallbackGenreSamples.put("Folk", Arrays.asList(
            new SongDto("Blowin' in the Wind", "Bob Dylan", "https://open.spotify.com/track/32x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/32d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Folk")
        ));
        fallbackGenreSamples.put("Smooth Jazz", Arrays.asList(
            new SongDto("Morning Dance", "Spyro Gyra", "https://open.spotify.com/track/42x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/42d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Smooth Jazz")
        ));
        fallbackGenreSamples.put("Big Band", Arrays.asList(
            new SongDto("In the Mood", "Glenn Miller", "https://open.spotify.com/track/52x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/52d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Big Band")
        ));
        fallbackGenreSamples.put("Soul", Arrays.asList(
            new SongDto("What's Going On", "Marvin Gaye", "https://open.spotify.com/track/62x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/62d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Soul")
        ));
        fallbackGenreSamples.put("Funk", Arrays.asList(
            new SongDto("Super Freak", "Rick James", "https://open.spotify.com/track/72x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/72d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Funk")
        ));
        fallbackGenreSamples.put("Disco", Arrays.asList(
            new SongDto("Stayin' Alive", "Bee Gees", "https://open.spotify.com/track/82x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/82d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Disco")
        ));
        fallbackGenreSamples.put("Punk", Arrays.asList(
            new SongDto("Blitzkrieg Bop", "Ramones", "https://open.spotify.com/track/92x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/92d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Punk")
        ));
        fallbackGenreSamples.put("Grunge", Arrays.asList(
            new SongDto("Smells Like Teen Spirit", "Nirvana", "https://open.spotify.com/track/13x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/13d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Grunge")
        ));
        fallbackGenreSamples.put("Metalcore", Arrays.asList(
            new SongDto("The End of Heartache", "Killswitch Engage", "https://open.spotify.com/track/23x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/23d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Metalcore")
        ));
        fallbackGenreSamples.put("Death Metal", Arrays.asList(
            new SongDto("Hammer Smashed Face", "Cannibal Corpse", "https://open.spotify.com/track/33x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/33d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Death Metal")
        ));
        fallbackGenreSamples.put("Black Metal", Arrays.asList(
            new SongDto("Freezing Moon", "Mayhem", "https://open.spotify.com/track/43x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/43d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Black Metal")
        ));
        fallbackGenreSamples.put("Symphonic Metal", Arrays.asList(
            new SongDto("Nemo", "Nightwish", "https://open.spotify.com/track/53x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/53d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Symphonic Metal")
        ));
        fallbackGenreSamples.put("Progressive Rock", Arrays.asList(
            new SongDto("Comfortably Numb", "Pink Floyd", "https://open.spotify.com/track/63x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/63d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Progressive Rock")
        ));
        fallbackGenreSamples.put("Psychedelic Rock", Arrays.asList(
            new SongDto("Light My Fire", "The Doors", "https://open.spotify.com/track/73x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/73d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Psychedelic Rock")
        ));
        fallbackGenreSamples.put("Lo-fi", Arrays.asList(
            new SongDto("Lo-fi Study Beats", "Lofi Girl", "https://open.spotify.com/track/83x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/83d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Lo-fi")
        ));
        fallbackGenreSamples.put("Chillwave", Arrays.asList(
            new SongDto("Feel It All Around", "Washed Out", "https://open.spotify.com/track/93x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/93d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Chillwave")
        ));
        fallbackGenreSamples.put("Synthwave", Arrays.asList(
            new SongDto("Nightcall", "Kavinsky", "https://open.spotify.com/track/14x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/14d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Synthwave")
        ));
        fallbackGenreSamples.put("Trance", Arrays.asList(
            new SongDto("Adagio for Strings", "Tiësto", "https://open.spotify.com/track/24x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/24d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Trance")
        ));
        fallbackGenreSamples.put("House", Arrays.asList(
            new SongDto("One More Time", "Daft Punk", "https://open.spotify.com/track/34x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/34d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "House")
        ));
        fallbackGenreSamples.put("Techno", Arrays.asList(
            new SongDto("Insomnia", "Faithless", "https://open.spotify.com/track/44x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/44d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Techno")
        ));
        fallbackGenreSamples.put("Dubstep", Arrays.asList(
            new SongDto("Scary Monsters and Nice Sprites", "Skrillex", "https://open.spotify.com/track/54x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/54d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Dubstep")
        ));
        fallbackGenreSamples.put("Drum & Bass", Arrays.asList(
            new SongDto("Inner City Life", "Goldie", "https://open.spotify.com/track/64x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/64d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Drum & Bass")
        ));
        fallbackGenreSamples.put("Salsa", Arrays.asList(
            new SongDto("La Vida Es Un Carnaval", "Celia Cruz", "https://open.spotify.com/track/74x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/74d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Salsa")
        ));
        fallbackGenreSamples.put("Flamenco", Arrays.asList(
            new SongDto("Entre Dos Aguas", "Paco de Lucía", "https://open.spotify.com/track/84x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/84d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Flamenco")
        ));
        fallbackGenreSamples.put("Opera", Arrays.asList(
            new SongDto("Nessun Dorma", "Giacomo Puccini", "https://open.spotify.com/track/94x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/94d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Opera")
        ));
        fallbackGenreSamples.put("World Music", Arrays.asList(
            new SongDto("Pata Pata", "Miriam Makeba", "https://open.spotify.com/track/15x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/15d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "World Music")
        ));
        fallbackGenreSamples.put("Bollywood", Arrays.asList(
            new SongDto("Jai Ho!", "A.R. Rahman", "https://open.spotify.com/track/25x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/25d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Bollywood")
        ));
        fallbackGenreSamples.put("Anime OST", Arrays.asList(
            new SongDto("Gurenge", "LiSA", "https://open.spotify.com/track/35x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/35d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Anime OST")
        ));
        fallbackGenreSamples.put("Video Game OST", Arrays.asList(
            new SongDto("One-Winged Angel", "Final Fantasy VII", "https://open.spotify.com/track/45x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/45d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Video Game OST")
        ));
        fallbackGenreSamples.put("Film Score", Arrays.asList(
            new SongDto("Hedwig's Theme", "Harry Potter", "https://open.spotify.com/track/55x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/55d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Film Score")
        ));
        fallbackGenreSamples.put("Childrens Music", Arrays.asList(
            new SongDto("Baby Shark", "Pinkfong", "https://open.spotify.com/track/65x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/65d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Childrens Music")
        ));
        fallbackGenreSamples.put("Holiday Music", Arrays.asList(
            new SongDto("All I Want for Christmas Is You", "Mariah Carey", "https://open.spotify.com/track/75x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/75d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Holiday Music")
        ));
        fallbackGenreSamples.put("Spoken Word", Arrays.asList(
            new SongDto("The Raven", "Edgar Allan Poe", "https://open.spotify.com/track/85x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/85d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Spoken Word")
        ));
        fallbackGenreSamples.put("Comedy", Arrays.asList(
            new SongDto("Always Look on the Bright Side of Life", "Monty Python", "https://open.spotify.com/track/95x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/95d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Comedy")
        ));
        fallbackGenreSamples.put("Drill", Arrays.asList(
            new SongDto("I Don't Like", "Chief Keef", "https://open.spotify.com/track/16x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/16d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Drill")
        ));
        fallbackGenreSamples.put("Grime", Arrays.asList(
            new SongDto("Pow! (Forward)", "Lethal Bizzle", "https://open.spotify.com/track/26x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/26d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Grime")
        ));
        fallbackGenreSamples.put("K-R&B", Arrays.asList(
            new SongDto("Crush", "Crush", "https://open.spotify.com/track/36x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/36d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "K-R&B")
        ));
        fallbackGenreSamples.put("J-Rock", Arrays.asList(
            new SongDto("Guren no Yumiya", "Linked Horizon", "https://open.spotify.com/track/46x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/46d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "J-Rock")
        ));
        fallbackGenreSamples.put("Pop Punk", Arrays.asList(
            new SongDto("What's My Age Again?", "Blink-182", "https://open.spotify.com/track/56x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/56d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Pop Punk")
        ));
        fallbackGenreSamples.put("Emo", Arrays.asList(
            new SongDto("Welcome to the Black Parade", "My Chemical Romance", "https://open.spotify.com/track/66x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/66d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Emo")
        ));
        fallbackGenreSamples.put("Folk Punk", Arrays.asList(
            new SongDto("A Toast to the Future Kids!", "Days N' Daze", "https://open.spotify.com/track/76x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/76d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Folk Punk")
        ));
        fallbackGenreSamples.put("Indie Pop", Arrays.asList(
            new SongDto("Pumped Up Kicks", "Foster the People", "https://open.spotify.com/track/86x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/86d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Indie Pop")
        ));
        fallbackGenreSamples.put("Dream Pop", Arrays.asList(
            new SongDto("Space Song", "Beach House", "https://open.spotify.com/track/96x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/96d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Dream Pop")
        ));
        fallbackGenreSamples.put("Neoclassical", Arrays.asList(
            new SongDto("Nuvole Bianche", "Ludovico Einaudi", "https://open.spotify.com/track/17x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/17d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Neoclassical")
        ));
        fallbackGenreSamples.put("Choral", Arrays.asList(
            new SongDto("Hallelujah Chorus", "Handel's Messiah", "https://open.spotify.com/track/27x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/27d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Choral")
        ));
        fallbackGenreSamples.put("Spa Music", Arrays.asList(
            new SongDto("Weightless", "Marconi Union", "https://open.spotify.com/track/37x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/37d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Spa Music")
        ));
        fallbackGenreSamples.put("Nature Sounds", Arrays.asList(
            new SongDto("Rain Sounds for Sleep", "Nature Sounds", "https://open.spotify.com/track/47x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/47d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Nature Sounds")
        ));
        fallbackGenreSamples.put("ASMR", Arrays.asList(
            new SongDto("ASMR Tapping Sounds", "ASMR Darling", "https://open.spotify.com/track/57x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/57d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "ASMR")
        ));
        fallbackGenreSamples.put("Educational Music", Arrays.asList(
            new SongDto("The Alphabet Song", "Traditional", "https://open.spotify.com/track/67x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/67d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Educational Music")
        ));
        fallbackGenreSamples.put("Novelty Songs", Arrays.asList(
            new SongDto("The Hamsterdance Song", "Hampton the Hamster", "https://open.spotify.com/track/77x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/77d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Novelty Songs")
        ));
        fallbackGenreSamples.put("Political Hip Hop", Arrays.asList(
            new SongDto("Fight the Power", "Public Enemy", "https://open.spotify.com/track/87x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/87d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Political Hip Hop")
        ));
        fallbackGenreSamples.put("Power Metal", Arrays.asList(
            new SongDto("The Bard's Song (In the Forest)", "Blind Guardian", "https://open.spotify.com/track/97x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/97d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Power Metal")
        ));
        fallbackGenreSamples.put("Viking Metal", Arrays.asList(
            new SongDto("Twilight of the Thunder God", "Amon Amarth", "https://open.spotify.com/track/18x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/18d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Viking Metal")
        ));
        fallbackGenreSamples.put("Pirate Metal", Arrays.asList(
            new SongDto("Keelhauled", "Alestorm", "https://open.spotify.com/track/28x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/28d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Pirate Metal")
        ));
        fallbackGenreSamples.put("Medieval Metal", Arrays.asList(
            new SongDto("In Taberna", "Corvus Corax", "https://open.spotify.com/track/38x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/38d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Medieval Metal")
        ));
        fallbackGenreSamples.put("Pagan Metal", Arrays.asList(
            new SongDto("Korpiklaani - Vodka", "Korpiklaani", "https://open.spotify.com/track/48x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/48d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Pagan Metal")
        ));
        fallbackGenreSamples.put("Blackened Thrash Metal", Arrays.asList(
            new SongDto("Total Destruction", "Desaster", "https://open.spotify.com/track/58x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/58d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Blackened Thrash Metal")
        ));
        fallbackGenreSamples.put("Technical Death Metal", Arrays.asList(
            new SongDto("Crystal Mountain", "Death", "https://open.spotify.com/track/68x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/68d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Technical Death Metal")
        ));
        fallbackGenreSamples.put("Brutal Death Metal", Arrays.asList(
            new SongDto("Hammer Smashed Face", "Cannibal Corpse", "https://open.spotify.com/track/78x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/78d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Brutal Death Metal")
        ));
        fallbackGenreSamples.put("Slam Death Metal", Arrays.asList(
            new SongDto("Hammer Smashed Face", "Cannibal Corpse", "https://open.spotify.com/track/88x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/88d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Slam Death Metal")
        ));
        fallbackGenreSamples.put("Progressive Death Metal", Arrays.asList(
            new SongDto("Crystal Mountain", "Death", "https://open.spotify.com/track/98x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/98d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Progressive Death Metal")
        ));
        fallbackGenreSamples.put("Melodic Death Metal", Arrays.asList(
            new SongDto("Blinded by Fear", "At the Gates", "https://open.spotify.com/track/19x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/19d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Melodic Death Metal")
        ));
        fallbackGenreSamples.put("Funeral Doom Metal", Arrays.asList(
            new SongDto("The Dreadful Hours", "My Dying Bride", "https://open.spotify.com/track/29x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/29d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Funeral Doom Metal")
        ));
        fallbackGenreSamples.put("Atmospheric Black Metal", Arrays.asList(
            new SongDto("Lost Wisdom", "Burzum", "https://open.spotify.com/track/39x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/39d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Atmospheric Black Metal")
        ));
        fallbackGenreSamples.put("Depressive Suicidal Black Metal (DSBM)", Arrays.asList(
            new SongDto("Suicide Is Painless", "Shining", "https://open.spotify.com/track/49x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/49d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Depressive Suicidal Black Metal (DSBM)")
        ));
        fallbackGenreSamples.put("Post-Black Metal", Arrays.asList(
            new SongDto("Sunbather", "Deafheaven", "https://open.spotify.com/track/59x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/59d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Post-Black Metal")
        ));
        fallbackGenreSamples.put("Raw Black Metal", Arrays.asList(
            new SongDto("Transilvanian Hunger", "Darkthrone", "https://open.spotify.com/track/69x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/69d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Raw Black Metal")
        ));
        fallbackGenreSamples.put("Blackgaze", Arrays.asList(
            new SongDto("Sunbather", "Deafheaven", "https://open.spotify.com/track/79x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/79d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Blackgaze")
        ));
        fallbackGenreSamples.put("Industrial Black Metal", Arrays.asList(
            new SongDto("The Grand Declaration of War", "Mayhem", "https://open.spotify.com/track/89x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/89d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Industrial Black Metal")
        ));
        fallbackGenreSamples.put("Folk Black Metal", Arrays.asList(
            new SongDto("Korpiklaani - Vodka", "Korpiklaani", "https://open.spotify.com/track/99x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/99d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Folk Black Metal")
        ));
        fallbackGenreSamples.put("War Metal", Arrays.asList(
            new SongDto("F.O.A.D.", "Goatwhore", "https://open.spotify.com/track/10x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/10d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "War Metal")
        ));
        fallbackGenreSamples.put("Powerviolence", Arrays.asList(
            new SongDto("You Suffer", "Napalm Death", "https://open.spotify.com/track/20x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/20d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Powerviolence")
        ));
        fallbackGenreSamples.put("No Wave", Arrays.asList(
            new SongDto("Contort Yourself", "James Chance and the Contortions", "https://open.spotify.com/track/30x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/30d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "No Wave")
        ));
        fallbackGenreSamples.put("Free Improvisation", Arrays.asList(
            new SongDto("Machine Gun", "Peter Brötzmann Octet", "https://open.spotify.com/track/40x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/40d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Free Improvisation")
        ));
        fallbackGenreSamples.put("Experimental Rock", Arrays.asList(
            new SongDto("I Am the Walrus", "The Beatles", "https://open.spotify.com/track/50x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/50d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Experimental Rock")
        ));
        fallbackGenreSamples.put("Avant-garde Metal", Arrays.asList(
            new SongDto("The Great Southern Trendkill", "Pantera", "https://open.spotify.com/track/60x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/60d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Avant-garde Metal")
        ));
        fallbackGenreSamples.put("Drone Metal", Arrays.asList(
            new SongDto("Monoliths & Dimensions", "Sunn O)))", "https://open.spotify.com/track/70x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/70d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Drone Metal")
        ));
        fallbackGenreSamples.put("Sludgecore", Arrays.asList(
            new SongDto("Take as Needed for Pain", "Eyehategod", "https://open.spotify.com/track/80x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/80d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Sludgecore")
        ));
        fallbackGenreSamples.put("Post-Metal", Arrays.asList(
            new SongDto("Panopticon", "Isis", "https://open.spotify.com/track/90x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/90d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Post-Metal")
        ));
        fallbackGenreSamples.put("Stoner Doom", Arrays.asList(
            new SongDto("Dopesmoker", "Sleep", "https://open.spotify.com/track/11x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/11d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Stoner Doom")
        ));
        fallbackGenreSamples.put("Psychedelic Doom", Arrays.asList(
            new SongDto("Dopesmoker", "Sleep", "https://open.spotify.com/track/21x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/21d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Psychedelic Doom")
        ));
        fallbackGenreSamples.put("Traditional Doom Metal", Arrays.asList(
            new SongDto("Black Sabbath", "Black Sabbath", "https://open.spotify.com/track/31x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/31d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Traditional Doom Metal")
        ));
        fallbackGenreSamples.put("Epic Doom Metal", Arrays.asList(
            new SongDto("Solitude", "Candlemass", "https://open.spotify.com/track/41x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/41d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Epic Doom Metal")
        ));
        fallbackGenreSamples.put("Folk Doom Metal", Arrays.asList(
            new SongDto("The Dreadful Hours", "My Dying Bride", "https://open.spotify.com/track/51x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/51d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Folk Doom Metal")
        ));
        fallbackGenreSamples.put("Progressive Doom Metal", Arrays.asList(
            new SongDto("Blackwater Park", "Opeth", "https://open.spotify.com/track/61x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/61d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Progressive Doom Metal")
        ));
        fallbackGenreSamples.put("Sludge Doom Metal", Arrays.asList(
            new SongDto("Take as Needed for Pain", "Eyehategod", "https://open.spotify.com/track/71x6s7f7f7f7f7f7f7f7f7f7f", "https://p.scdn.co/mp3-preview/71d8d6d5c2c06e2e5e8e8e8e8e8e8e8e8e8e8e8e", "Sludge Doom Metal")
        ));


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