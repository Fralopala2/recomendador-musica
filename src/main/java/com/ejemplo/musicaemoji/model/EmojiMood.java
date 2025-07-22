    // EmojiMood.java
    // Ruta: src/main/java/com/ejemplo/musicaemoji/model/EmojiMood.java

    package com.ejemplo.musicaemoji.model;

    import jakarta.persistence.*; // Usa jakarta.persistence para Spring Boot 3+
    import lombok.Data; // Importa Lombok para getters/setters/constructores automáticos
    import lombok.NoArgsConstructor;
    import lombok.AllArgsConstructor;

    @Entity // Indica que esta clase es una entidad JPA y se mapea a una tabla de base de datos
    @Table(name = "emoji_mood") // Opcional: especifica el nombre de la tabla si es diferente al nombre de la clase
    @Data // Anotación de Lombok para generar getters, setters, toString, equals y hashCode
    @NoArgsConstructor // Anotación de Lombok para generar un constructor sin argumentos
    @AllArgsConstructor // Anotación de Lombok para generar un constructor con todos los argumentos
    public class EmojiMood {

        @Id // Marca el campo como la clave primaria de la tabla
        @GeneratedValue(strategy = GenerationType.IDENTITY) // Configura la generación automática de IDs (autoincremento)
        private Long id;

        @Column(nullable = false, unique = true) // Configura la columna 'emoji': no puede ser nula y debe ser única
        private String emoji;

        @Column(name = "mood_description") // Opcional: especifica el nombre de la columna si es diferente al nombre del campo
        private String moodDescription;

        @Column(name = "genre_hint") // Opcional: especifica el nombre de la columna si es diferente al nombre del campo
        private String genreHint;
    }