package com.ejemplo.musicaemoji.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SpotifyService {

    // URL base de la API de autenticación de Spotify
    private static final String SPOTIFY_AUTH_URL = "https://accounts.spotify.com/api/token";
    // URL base de la API de Spotify
    private static final String SPOTIFY_API_URL = "https://api.spotify.com/v1";

    // Inyecta el Client ID y Client Secret desde las variables de entorno de Spring Boot
    @Value("${spotify.client.id}")
    private String clientId;

    @Value("${spotify.client.secret}")
    private String clientSecret;

    // WebClient para realizar peticiones HTTP
    private final WebClient webClient;

    // Cache para almacenar el token de acceso de Spotify y su tiempo de expiración
    // Usamos ConcurrentHashMap para ser thread-safe
    private final ConcurrentHashMap<String, String> tokenCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> tokenExpiry = new ConcurrentHashMap<>();

    public SpotifyService(WebClient.Builder webClientBuilder) {
        // Construye el WebClient con la URL base de la API de Spotify
        this.webClient = webClientBuilder.baseUrl(SPOTIFY_API_URL).build();
    }

    /**
     * Obtiene un token de acceso de Spotify utilizando el Client Credentials Flow.
     * El token se cachea y se refresca cuando expira.
     * @return Mono<String> que emite el token de acceso
     */
    private Mono<String> getAccessToken() {
        // Comprueba si el token cacheado es válido y no ha expirado
        String cachedToken = tokenCache.get("spotify_access_token");
        Long expiryTime = tokenExpiry.get("spotify_access_token");

        if (cachedToken != null && expiryTime != null && System.currentTimeMillis() < expiryTime) {
            return Mono.just(cachedToken); // Devuelve el token cacheado si es válido
        }

        // Si no hay token válido, solicita uno nuevo
        String authString = clientId + ":" + clientSecret;
        String encodedAuthString = Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));

        return WebClient.builder().baseUrl(SPOTIFY_AUTH_URL).build()
                .post()
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuthString)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "client_credentials"))
                .retrieve()
                .bodyToMono(JsonNode.class) // Convierte la respuesta a un objeto JsonNode
                .map(jsonNode -> {
                    String accessToken = jsonNode.get("access_token").asText();
                    long expiresIn = jsonNode.get("expires_in").asLong(); // Tiempo en segundos
                    long newExpiryTime = System.currentTimeMillis() + (expiresIn * 1000) - 5000; // 5 segundos de margen

                    // Almacena el nuevo token y su tiempo de expiración en la cache
                    tokenCache.put("spotify_access_token", accessToken);
                    tokenExpiry.put("spotify_access_token", newExpiryTime);
                    return accessToken;
                });
    }

    /**
     * Busca canciones en Spotify basándose en una consulta y un tipo (track, playlist, etc.).
     * @param query La cadena de búsqueda (ej. "Pop hits", "Rock anthems").
     * @param type El tipo de elemento a buscar (ej. "track", "playlist").
     * @param limit El número máximo de resultados a devolver.
     * @return Mono<List<String>> que emite una lista de nombres de canciones o nombres de playlists con sus URLs.
     */
    public Mono<List<String>> searchSpotify(String query, String type, int limit) {
        return getAccessToken().flatMap(accessToken ->
            webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/search")
                            .queryParam("q", URLEncoder.encode(query, StandardCharsets.UTF_8)) // Codifica la query
                            .queryParam("type", type)
                            .queryParam("limit", limit)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(JsonNode.class) // Convierte la respuesta a un objeto JsonNode
                    .map(jsonNode -> {
                        List<String> results = new ArrayList<>();
                        // Procesa los resultados según el tipo de búsqueda
                        if ("track".equals(type)) {
                            JsonNode tracksNode = jsonNode.path("tracks").path("items");
                            for (JsonNode track : tracksNode) {
                                String songName = track.path("name").asText();
                                String artistName = track.path("artists").get(0).path("name").asText();
                                String externalUrl = track.path("external_urls").path("spotify").asText();
                                results.add(songName + " - " + artistName + " (" + externalUrl + ")");
                            }
                        } else if ("playlist".equals(type)) {
                            JsonNode playlistsNode = jsonNode.path("playlists").path("items");
                            for (JsonNode playlist : playlistsNode) {
                                String playlistName = playlist.path("name").asText();
                                String externalUrl = playlist.path("external_urls").path("spotify").asText();
                                results.add("Playlist: " + playlistName + " (" + externalUrl + ")");
                            }
                        }
                        return results;
                    })
                    .onErrorResume(e -> {
                        // Manejo de errores en la búsqueda
                        System.err.println("Error searching Spotify: " + e.getMessage());
                        return Mono.just(Collections.emptyList()); // Devuelve una lista vacía en caso de error
                    })
        ).onErrorResume(e -> {
            // Manejo de errores al obtener el token
            System.err.println("Error getting Spotify access token: " + e.getMessage());
            return Mono.just(Collections.emptyList()); // Devuelve una lista vacía
        });
    }
}