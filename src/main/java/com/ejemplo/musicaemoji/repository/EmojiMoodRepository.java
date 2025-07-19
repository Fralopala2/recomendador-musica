package com.ejemplo.musicaemoji.repository;

import com.ejemplo.musicaemoji.model.EmojiMood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository // Indica que esta interfaz es un componente de repositorio
public interface EmojiMoodRepository extends JpaRepository<EmojiMood, Long> {
    // Método para encontrar un mapeo por el emoji exacto
    Optional<EmojiMood> findByEmoji(String emoji);
    // Métodos para buscar por descripción o género (útiles para lógicas más avanzadas)
    List<EmojiMood> findByMoodDescriptionContainingIgnoreCase(String moodDescription);
    List<EmojiMood> findByGenreHintContainingIgnoreCase(String genreHint);
}