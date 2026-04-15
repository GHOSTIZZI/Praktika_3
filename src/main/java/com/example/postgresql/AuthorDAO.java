package com.example.postgresql;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AuthorDAO extends SupabaseDAO {

    public CompletableFuture<List<Author>> getAllAuthors() {
        HttpRequest request = createBaseRequestBuilder("/authors?order=name.asc").GET().build();

        final List<Author> EMPTY_AUTHOR_LIST = Collections.emptyList();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            return objectMapper.readValue(response.body(), new TypeReference<List<Author>>() {});
                        } catch (IOException e) {
                            e.printStackTrace();
                            return EMPTY_AUTHOR_LIST;
                        }
                    } else {
                        handleDaoError(response, "получении списка авторов");
                        return EMPTY_AUTHOR_LIST;
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Сетевая ошибка при получении авторов: " + e.getMessage());
                    return EMPTY_AUTHOR_LIST;
                });
    }

    public CompletableFuture<Integer> getAuthorIdByName(String name) {
        String encodedName = encodeUrlParameter(name);

        HttpRequest request = createBaseRequestBuilder("/authors?select=id&name=ilike." + encodedName).GET().build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            JsonNode root = objectMapper.readTree(response.body());
                            if (root.isArray() && root.size() > 0) {

                                return root.get(0).get("id").asInt();
                            }
                            return 0;
                        } catch (IOException e) {
                            e.printStackTrace();
                            return 0;
                        }
                    } else {
                        handleDaoError(response, "получении ID автора");
                        return 0;
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Сетевая ошибка при получении ID автора: " + e.getMessage());
                    return 0;
                });
    }

    public CompletableFuture<List<Author>> searchAuthorsByName(String query) {
        String encodedQuery = encodeUrlParameter(query);

        HttpRequest request = createBaseRequestBuilder("/authors?name=ilike.*" + encodedQuery + "*&order=name.asc")
                .GET()
                .build();

        final List<Author> EMPTY_AUTHOR_LIST = Collections.emptyList();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            return objectMapper.readValue(response.body(), new TypeReference<List<Author>>() {});
                        } catch (IOException e) {
                            e.printStackTrace();
                            return EMPTY_AUTHOR_LIST;
                        }
                    } else {
                        handleDaoError(response, "поиске авторов");
                        return EMPTY_AUTHOR_LIST;
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Сетевая ошибка при поиске авторов: " + e.getMessage());
                    return EMPTY_AUTHOR_LIST;
                });
    }
    public CompletableFuture<Boolean> addAuthor(String name, String photoUrl) {
        ObjectNode jsonBody = objectMapper.createObjectNode()
                .put("name", name)
                .put("photo_url", photoUrl);

        return sendPostRequest("/authors", jsonBody, "добавлении нового автора: " + name);
    }

    public CompletableFuture<Boolean> deleteAuthor(int authorId) {
        String pathQuery = "/authors?id=eq." + authorId;
        return sendDeleteRequest(pathQuery, "удалении автора с ID " + authorId);
    }

}