package com.example.postgresql;

import java.util.List;

public class SearchResult {
    private final List<Track> tracks;
    private final List<Author> authors;

    public SearchResult(List<Track> tracks, List<Author> authors) {
        this.tracks = tracks;
        this.authors = authors;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public List<Author> getAuthors() {
        return authors;
    }
}
