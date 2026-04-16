package com.example.postgresql;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PlaylistDAO extends SupabaseDAO {

    private static final String PLAYLISTS = "/playlists";
    private static final String PLAYLIST_TRACKS = "/playlist_tracks";


    public CompletableFuture<List<Playlist>> getUserPlaylists(int userId) {

        String query = PLAYLISTS + "?owner_id=eq." + userId + "&is_featured=eq.false" + "&order=title.asc";

        HttpRequest request = createBaseRequestBuilder(query).GET().build();

        return sendAndDeserializeList(
                request,
                new TypeReference<List<Playlist>>() {},
                "получении плейлистов пользователя"
        );
    }


    public CompletableFuture<List<Playlist>> getFeaturedPlaylists() {
        String query = PLAYLISTS + "?is_featured=eq.true&order=title.asc";

        HttpRequest request = createBaseRequestBuilder(query).GET().build();

        return sendAndDeserializeList(
                request,
                new TypeReference<List<Playlist>>() {},
                "получении featured плейлистов"
        );
    }

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

    public CompletableFuture<Boolean> deletePlaylist(int playlistId) {
        return sendDeleteRequest(
                PLAYLISTS + "?id=eq." + playlistId,
                "удалении плейлиста"
        );
    }

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

}