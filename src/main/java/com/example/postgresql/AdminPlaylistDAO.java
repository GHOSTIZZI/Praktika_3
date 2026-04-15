package com.example.postgresql;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AdminPlaylistDAO {

    private final PlaylistDAO playlistDAO = new PlaylistDAO();

    private static final int ADMIN_OWNER_ID = 1;

    public CompletableFuture<Boolean> createAdminAlbum(
            String title,
            String coverUrl,
            List<Track> selectedTracks
    ) {

        return playlistDAO.createPlaylist(
                title,
                coverUrl,
                ADMIN_OWNER_ID,
                true
        ).thenCompose(success -> {
            if (!success) {
                return CompletableFuture.completedFuture(false);
            }

            return playlistDAO.getFeaturedPlaylists()
                    .thenCompose(playlists -> {

                        Playlist created = playlists.stream()
                                .filter(p -> p.getTitle().equals(title))
                                .findFirst()
                                .orElse(null);

                        if (created == null) {
                            return CompletableFuture.completedFuture(false);
                        }

                        CompletableFuture<Boolean> chain =
                                CompletableFuture.completedFuture(true);

                        for (Track track : selectedTracks) {
                            chain = chain.thenCompose(r ->
                                    playlistDAO.addTrackToPlaylist(
                                            created.getId(),
                                            track.getId()
                                    )
                            );
                        }

                        return chain;
                    });
        });
    }
}