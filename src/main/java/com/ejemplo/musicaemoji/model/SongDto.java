package com.ejemplo.musicaemoji.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Anotación de Lombok para generar getters, setters, toString, equals y hashCode
@NoArgsConstructor // Anotación de Lombok para generar un constructor sin argumentos
@AllArgsConstructor // Anotación de Lombok para generar un constructor con todos los argumentos
public class SongDto {
    private String id; // ID del documento de Firestore (puede ser nulo para canciones de Spotify)
    private String name;
    private String artist;
    private String spotifyUrl;
    private String previewUrl;
    private String recommendedGenre; // Nuevo campo para el género recomendado
}