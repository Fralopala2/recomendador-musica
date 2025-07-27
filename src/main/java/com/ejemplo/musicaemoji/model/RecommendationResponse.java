package com.ejemplo.musicaemoji.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data // Anotación de Lombok para generar getters, setters, toString, equals y hashCode
@NoArgsConstructor // Anotación de Lombok para generar un constructor sin argumentos
@AllArgsConstructor // Anotación de Lombok para generar un constructor con todos los argumentos
public class RecommendationResponse {
    private List<SongDto> songs;
    private Set<String> genres;

    // Si no usas Lombok, necesitarías añadir un constructor manual como este:
    /*
    public RecommendationResponse(List<SongDto> songs, Set<String> genres) {
        this.songs = songs;
        this.genres = genres;
    }
    */
}