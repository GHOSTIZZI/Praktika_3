package com.example.postgresql;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.http.HttpRequest;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class TrackDAO extends SupabaseDAO {

    private static final String TRACKS_TABLE = "/tracks";
    private static final String AUTHOR_SELECT = "*,author:authors(name)";

    private String mapSortKey(String javaSortKey) {
        return "title.asc";
    }

    public CompletableFuture<List<Track>> getAllTracks(String orderBy) {
        String cleanedOrderBy = "title.asc";

        String query = TRACKS_TABLE + "?select=" + AUTHOR_SELECT + "&order=" + cleanedOrderBy;
        HttpRequest request = createBaseRequestBuilder(query).GET().build();
        return sendAndDeserializeList(request, new TypeReference<List<Track>>() {}, "получении списка всех треков");
    }

    public CompletableFuture<List<Track>> getAllTracks() {
        return getAllTracks("title.asc");
    }

    public CompletableFuture<List<Track>> getTracksByAuthorId(int authorId) {
        String query = TRACKS_TABLE + "?select=" + AUTHOR_SELECT + "&author_id=eq." + authorId + "&order=title.asc";

        HttpRequest request = createBaseRequestBuilder(query).GET().build();
        return sendAndDeserializeList(request, new TypeReference<List<Track>>() {}, "получении треков по ID автора");
    }

    public CompletableFuture<List<Track>> searchTracks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        String trimmed = query.trim();
        String encoded = encodeUrlParameter(trimmed);
        String wildcard = "%25" + encoded + "%25";

        String queryByTitle = TRACKS_TABLE + "?select=" + AUTHOR_SELECT + "&title=ilike.*" + wildcard + "&order=title.asc";
        HttpRequest requestByTitle = createBaseRequestBuilder(queryByTitle).GET().build();

        return sendAndDeserializeList(
                requestByTitle,
                new TypeReference<List<Track>>() {},
                "поиск треков по названию"
        );
    }


    public CompletableFuture<List<Track>> getFavoriteTracksByUser(int userId, String orderBy) {

        String query = TRACKS_TABLE + "?select=" + AUTHOR_SELECT + ",favorites!inner()&" + "favorites.user_id=eq." + userId;

        HttpRequest request = createBaseRequestBuilder(query).GET().build();
        return sendAndDeserializeList(request, new TypeReference<List<Track>>() {}, "получении избранных треков");
    }

    public CompletableFuture<Boolean> addTrack(Track track) {
        ObjectNode jsonBody = objectMapper.createObjectNode()
                .put("title", track.getTitle())
                .put("author_id", track.getAuthorId())
                .put("album", track.getAlbum())
                .put("genre", track.getGenre())
                .put("cover_url", track.getCoverUrl())
                .put("audio_url", track.getTrackUrl());

        return sendPostRequest(TRACKS_TABLE, jsonBody, "добавлении нового трека: " + track.getTitle());
    }



    public CompletableFuture<Boolean> deleteTrack(int trackId) {
        String pathQuery = TRACKS_TABLE + "?id=eq." + trackId;

        return sendDeleteRequest(pathQuery, "удалении трека с ID " + trackId);
    }
}