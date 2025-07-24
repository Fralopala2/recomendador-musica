package com.ejemplo.musicaemoji.model;

import java.util.List;
import java.util.Set;

public class RecommendationResponse {
    private Set<String> genres;
    private List<String> songs;

    // Constructor vacío
    public RecommendationResponse() {
    }

    // Constructor con todos los campos
    public RecommendationResponse(Set<String> genres, List<String> songs) {
        this.genres = genres;
        this.songs = songs;
    }

    // Getters
    public Set<String> getGenres() {
        return genres;
    }

    public List<String> getSongs() {
        return songs;
    }

    // Setters
    public void setGenres(Set<String> genres) {
        this.genres = genres;
    }

    public void setSongs(List<String> songs) {
        this.songs = songs;
    }
}