package com.example.postgresql;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Track {

    private int id;
    private String title;
    private String artist;

    private String coverUrl;
    private int authorId;
    private String album;
    private String genre;
    private String trackUrl;
    private String audioUrl;
    private String authorName;


    public String getAuthorName() {
        return authorName;
    }


    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
    public Track() {
    }


    public Track(int id, String title, String artist, String duration, String coverUrl, String trackUrl, int authorId, String album, String genre) {
        this.id = id;
        this.title = title;
        this.artist = artist;

        this.coverUrl = coverUrl;
        this.trackUrl = trackUrl;
        this.audioUrl = trackUrl;
        this.authorId = authorId;
        this.album = album;
        this.genre = genre;
    }

    // ===================================
    // СПЕЦИАЛЬНЫЙ СЕТТЕР ДЛЯ ДЕСЕРИАЛИЗАЦИИ АВТОРА
    // ===================================


    @JsonProperty("author")
    public void setAuthorObject(JsonNode authorNode) {
        if (authorNode != null && authorNode.has("name")) {
            // Извлекаем текстовое значение 'name' из вложенного JSON-узла
            this.artist = authorNode.get("name").asText();
        } else {
            this.artist = "Неизвестный автор";
        }
    }

    // ===================================
    // ГЕТТЕРЫ И СЕТТЕРЫ
    // ===================================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }


    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }


    public String getAlbum() { return album; }
    @JsonProperty("album")
    public void setAlbum(String album) { this.album = album; }


    public String getGenre() { return genre; }
    @JsonProperty("genre")
    public void setGenre(String genre) { this.genre = genre; }


    public int getAuthorId() { return authorId; }
    @JsonProperty("author_id")
    public void setAuthorId(int authorId) { this.authorId = authorId; }


    @JsonProperty("cover_url")
    public String getCoverUrl() { return coverUrl; }
    @JsonProperty("cover_url")
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

    @JsonProperty("audio_url")
    public String getTrackUrl() { return trackUrl; }

    @JsonProperty("audio_url")
    public void setTrackUrl(String trackUrl) {
        this.trackUrl = trackUrl;
        this.audioUrl = trackUrl;
    }

    @Override
    public String toString() {
        return id + " | " + title + " - " + artist;
    }
}