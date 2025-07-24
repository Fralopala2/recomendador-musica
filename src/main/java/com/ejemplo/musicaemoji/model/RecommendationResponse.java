package com.ejemplo.musicaemoji.model;

import java.util.List;
import java.util.Set;

public class RecommendationResponse {
    private Set<String> genres;
    private List<SongDto> songs; // CAMBIO: Ahora es una lista de SongDto

    // Constructor vacío
    public RecommendationResponse() {
    }

    // Constructor con todos los campos
    public RecommendationResponse(Set<String> genres, List<SongDto> songs) { // CAMBIO: Tipo de 'songs'
        this.genres = genres;
        this.songs = songs;
    }

    // Getters
    public Set<String> getGenres() {
        return genres;
    }

    public List<SongDto> getSongs() { // CAMBIO: Tipo de retorno
        return songs;
    }

    // Setters
    public void setGenres(Set<String> genres) {
        this.genres = genres;
    }

    public void setSongs(List<SongDto> songs) { // CAMBIO: Tipo de parámetro
        this.songs = songs;
    }
}
