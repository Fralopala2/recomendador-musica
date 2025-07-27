package com.ejemplo.musicaemoji.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Anotación de Lombok para generar getters, setters, toString, equals y hashCode
@NoArgsConstructor // Anotación de Lombok para generar un constructor sin argumentos
@AllArgsConstructor // Anotación de Lombok para generar un constructor con todos los argumentos
public class SongDto {
    private String name;
    private String artist;
    private String spotifyUrl;
    private String previewUrl; // Campo para la URL de vista previa
    private String recommendedGenre; // <-- NUEVO CAMPO: Género recomendado asociado a la canción
}