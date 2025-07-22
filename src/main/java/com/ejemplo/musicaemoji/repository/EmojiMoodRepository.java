// EmojiMoodRepository.java
    // Ruta: src/main/java/com/ejemplo/musicaemoji/repository/EmojiMoodRepository.java

    package com.ejemplo.musicaemoji.repository;

    import com.ejemplo.musicaemoji.model.EmojiMood;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;

    import java.util.Optional;

    @Repository // Indica que esta interfaz es un componente de repositorio de Spring
    public interface EmojiMoodRepository extends JpaRepository<EmojiMood, Long> {
        // JpaRepository proporciona métodos CRUD básicos (save, findById, findAll, delete, etc.)

        // Método personalizado para buscar un EmojiMood por su emoji
        Optional<EmojiMood> findByEmoji(String emoji);
    }