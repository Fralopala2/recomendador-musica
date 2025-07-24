package com.ejemplo.musicaemoji.service;

import com.ejemplo.musicaemoji.model.SongDto; // Importa el nuevo SongDto
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

    private static final String SPOTIFY_AUTH_URL = "https://accounts.spotify.com/api/token";
    private static final String SPOTIFY_API_URL = "https://api.spotify.com/v1";

    @Value("${spotify.client.id}")
    private String clientId;

    @Value("${spotify.client.secret}")
    private String clientSecret;

    private final WebClient webClient;

    private final ConcurrentHashMap<String, String> tokenCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> tokenExpiry = new ConcurrentHashMap<>();

    public SpotifyService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(SPOTIFY_API_URL).build();
    }

    private Mono<String> getAccessToken() {
        String cachedToken = tokenCache.get("spotify_access_token");
        Long expiryTime = tokenExpiry.get("spotify_access_token");

        if (cachedToken != null && expiryTime != null && System.currentTimeMillis() < expiryTime) {
            return Mono.just(cachedToken);
        }

        String authString = clientId + ":" + clientSecret;
        String encodedAuthString = Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));

        return WebClient.builder().baseUrl(SPOTIFY_AUTH_URL).build()
                .post()
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuthString)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "client_credentials"))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(jsonNode -> {
                    String accessToken = jsonNode.get("access_token").asText();
                    long expiresIn = jsonNode.get("expires_in").asLong();
                    long newExpiryTime = System.currentTimeMillis() + (expiresIn * 1000) - 5000;

                    tokenCache.put("spotify_access_token", accessToken);
                    tokenExpiry.put("spotify_access_token", newExpiryTime);
                    return accessToken;
                });
    }

    /**
     * Busca canciones en Spotify y devuelve una lista de SongDto.
     * @param query La cadena de búsqueda.
     * @param type El tipo de elemento a buscar (ej. "track").
     * @param limit El número máximo de resultados a devolver.
     * @return Mono<List<SongDto>> que emite una lista de SongDto.
     */
    public Mono<List<SongDto>> searchSpotify(String query, String type, int limit) { // CAMBIO: Retorna Mono<List<SongDto>>
        return getAccessToken().flatMap(accessToken ->
            webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/search")
                            .queryParam("q", URLEncoder.encode(query, StandardCharsets.UTF_8))
                            .queryParam("type", type)
                            .queryParam("limit", limit)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .map(jsonNode -> {
                        List<SongDto> results = new ArrayList<>(); // CAMBIO: Lista de SongDto
                        if ("track".equals(type)) {
                            JsonNode tracksNode = jsonNode.path("tracks").path("items");
                            for (JsonNode track : tracksNode) {
                                String songName = track.path("name").asText();
                                String artistName = track.path("artists").get(0).path("name").asText();
                                String spotifyUrl = track.path("external_urls").path("spotify").asText();
                                String previewUrl = track.path("preview_url").asText(); // Extrae la URL de vista previa

                                // Crea un nuevo SongDto y añádelo a los resultados
                                results.add(new SongDto(songName, artistName, spotifyUrl, previewUrl));
                            }
                        }
                        // Puedes añadir lógica para otros tipos si es necesario, pero actualmente solo buscamos tracks.
                        return results;
                    })
                    .onErrorResume(e -> {
                        System.err.println("Error searching Spotify: " + e.getMessage());
                        return Mono.just(Collections.emptyList());
                    })
        ).onErrorResume(e -> {
            System.err.println("Error getting Spotify access token: " + e.getMessage());
            return Mono.just(Collections.emptyList());
        });
    }
}