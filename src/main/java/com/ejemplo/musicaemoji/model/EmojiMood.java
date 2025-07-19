package com.ejemplo.musicaemoji.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity // Indica que es una entidad JPA
@Data // Anotación de Lombok
@NoArgsConstructor // Anotación de Lombok
@AllArgsConstructor // Anotación de Lombok
public class EmojiMood {

    @Id // Clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID auto-generado
    private Long id;
    private String emoji; // El emoji en sí (ej: "😄")
    private String moodDescription; // Descripción del estado de ánimo (ej: "Alegre", "Fiesta")
    private String genreHint; // Sugerencia de género musical (ej: "Pop", "Dance")
}