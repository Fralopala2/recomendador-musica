package com.ejemplo.musicaemoji.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {
    // El orden de los campos en el constructor AllArgsConstructor es importante
    private Set<String> genres; // Primero el conjunto de géneros
    private List<SongDto> songs; // Luego la lista de canciones
}