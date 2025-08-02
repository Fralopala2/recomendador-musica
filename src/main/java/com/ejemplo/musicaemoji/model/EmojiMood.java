package com.ejemplo.musicaemoji.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Anotación de Lombok para generar getters, setters, toString, equals y hashCode
@NoArgsConstructor // Anotación de Lombok para generar un constructor sin argumentos
@AllArgsConstructor // Anotación de Lombok para generar un constructor con todos los argumentos
public class EmojiMood {
    // Para Firestore, el ID del documento se maneja por separado o se puede incluir como un campo.
    // No necesitamos @Id, @GeneratedValue, @Entity, @Table, @Column.
    // Firestore asigna IDs automáticamente a los documentos si no especificamos uno.
    private String id; // El ID del documento de Firestore (String)
    private String emoji;
    private String moodDescription;
    private String genreHint;
}