package com.example.postgresql;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;


public class FavoriteDAO extends SupabaseDAO {


    public CompletableFuture<Boolean> isFavorite(int userId, int trackId) {

        String query = String.format("/favorites?user_id=eq.%d&track_id=eq.%d&select=user_id&limit=1", userId, trackId);
        HttpRequest request = createBaseRequestBuilder(query).GET().build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {

                    if (response.statusCode() == 200) {

                        return !response.body().equals("[]");
                    } else {
                        handleDaoError(response, "проверке статуса избранного");
                        return false;
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Сетевая ошибка при проверке статуса избранного: " + e.getMessage());
                    return false;
                });
    }



    public CompletableFuture<Boolean> addToFavorites(int userId, int trackId) {

        ObjectNode jsonBody = objectMapper.createObjectNode()
                .put("user_id", userId)
                .put("track_id", trackId);

        HttpRequest request = createBaseRequestBuilder("/favorites")
                .header("Prefer", "resolution=merge-duplicates")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString()))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 201 || response.statusCode() == 200) {
                        return true;
                    } else {
                        handleDaoError(response, "добавлении в избранное");
                        return false;
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Сетевая ошибка при добавлении в избранное: " + e.getMessage());
                    return false;
                });
    }


    public CompletableFuture<Boolean> removeFromFavorites(int userId, int trackId) {

        String query = String.format("/favorites?user_id=eq.%d&track_id=eq.%d", userId, trackId);
        HttpRequest request = createBaseRequestBuilder(query)
                .DELETE()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 204) {
                        return true;
                    } else {
                        handleDaoError(response, "удалении из избранного");
                        return false;
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Сетевая ошибка при удалении из избранного: " + e.getMessage());
                    return false;
                });
    }
}