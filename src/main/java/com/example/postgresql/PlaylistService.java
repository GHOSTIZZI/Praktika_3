package com.example.postgresql;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PlaylistService {

    private final PlaylistDAO playlistDAO = new PlaylistDAO();

    public CompletableFuture<List<Playlist>> loadUserPlaylists(int userId) {
        return playlistDAO.getUserPlaylists(userId);
    }

    public CompletableFuture<List<Track>> loadPlaylistTracks(int playlistId) {
        return playlistDAO.getTracksByPlaylistId(playlistId);
    }

    public CompletableFuture<Boolean> createPlaylist(String title, String coverUrl, int userId) {
        return playlistDAO.createPlaylist(title, coverUrl, userId, false);
    }

    public CompletableFuture<Boolean> addTrack(int playlistId, int trackId) {
        return playlistDAO.addTrackToPlaylist(playlistId, trackId);
    }

    public CompletableFuture<Boolean> removeTrack(int playlistId, int trackId) {
        return playlistDAO.removeTrackFromPlaylist(playlistId, trackId);
    }

    public CompletableFuture<Boolean> deletePlaylist(int playlistId) {
        return playlistDAO.deletePlaylist(playlistId);
    }

    public CompletableFuture<List<Playlist>> loadFeaturedPlaylists() {
        return playlistDAO.getFeaturedPlaylists();
    }

}