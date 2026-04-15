package com.example.postgresql;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PlaylistDAO extends SupabaseDAO {

    private static final String PLAYLISTS = "/playlists";
    private static final String PLAYLIST_TRACKS = "/playlist_tracks";

    // ===================== GET USER PLAYLISTS =====================
    public CompletableFuture<List<Playlist>> getUserPlaylists(int userId) {

        String query = PLAYLISTS + "?owner_id=eq." + userId + "&is_featured=eq.false" + "&order=title.asc";

        HttpRequest request = createBaseRequestBuilder(query).GET().build();

        return sendAndDeserializeList(
                request,
                new TypeReference<List<Playlist>>() {},
                "получении плейлистов пользователя"
        );
    }

    // ===================== GET FEATURED (ADMIN) =====================
    public CompletableFuture<List<Playlist>> getFeaturedPlaylists() {
        String query = PLAYLISTS + "?is_featured=eq.true&order=title.asc";

        HttpRequest request = createBaseRequestBuilder(query).GET().build();

        return sendAndDeserializeList(
                request,
                new TypeReference<List<Playlist>>() {},
                "получении featured плейлистов"
        );
    }

    // ===================== CREATE PLAYLIST =====================
    public CompletableFuture<Boolean> createPlaylist(String title, String coverUrl, int ownerId, boolean isFeatured) {

        if (title == null || title.trim().isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }

        ObjectNode json = objectMapper.createObjectNode()
                .put("title", title)
                .put("cover_url", coverUrl)
                .put("owner_id", ownerId)
                .put("is_featured", isFeatured);

        return sendPostRequest(PLAYLISTS, json, "создании плейлиста");
    }

    // ===================== DELETE PLAYLIST =====================
    public CompletableFuture<Boolean> deletePlaylist(int playlistId) {
        return sendDeleteRequest(
                PLAYLISTS + "?id=eq." + playlistId,
                "удалении плейлиста"
        );
    }

    // ===================== ADD TRACK =====================
    public CompletableFuture<Boolean> addTrackToPlaylist(int playlistId, int trackId) {

        ObjectNode json = objectMapper.createObjectNode()
                .put("playlist_id", playlistId)
                .put("track_id", trackId);

        return sendPostRequest(
                PLAYLIST_TRACKS,
                json,
                "добавлении трека в плейлист"
        );
    }

    // ===================== REMOVE TRACK =====================
    public CompletableFuture<Boolean> removeTrackFromPlaylist(int playlistId, int trackId) {
        return sendDeleteRequest(
                PLAYLIST_TRACKS + "?playlist_id=eq." + playlistId + "&track_id=eq." + trackId,
                "удалении трека из плейлиста"
        );
    }
    public CompletableFuture<List<Track>> getTracksByPlaylistId(int playlistId) {

        String query =
                "/tracks?select=*,author:authors(name),playlist_tracks!inner()" +
                        "&playlist_tracks.playlist_id=eq." + playlistId;

        HttpRequest request = createBaseRequestBuilder(query).GET().build();

        return sendAndDeserializeList(
                request,
                new TypeReference<List<Track>>() {},
                "получении треков плейлиста"
        );
    }

    public CompletableFuture<Boolean> isTrackInPlaylist(int playlistId, int trackId) {

        String query = "/playlist_tracks?playlist_id=eq." + playlistId +
                "&track_id=eq." + trackId + "&select=track_id&limit=1";

        HttpRequest request = createBaseRequestBuilder(query).GET().build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return !response.body().equals("[]");
                    }
                    return false;
                });
    }



    public CompletableFuture<Track> getFirstTrackInPlaylist(int playlistId) {

        String query =
                "/playlist_tracks?playlist_id=eq." + playlistId +
                        "&select=track:tracks(*)" +
                        "&order=id.asc&limit=1";

        HttpRequest request = createBaseRequestBuilder(query).GET().build();

        return sendAndDeserializeList(
                request,
                new TypeReference<List<Track>>() {},
                "получении первого трека плейлиста"
        ).thenApply(list -> {
            if (list.isEmpty()) return null;
            return list.get(0);
        });
    }


}