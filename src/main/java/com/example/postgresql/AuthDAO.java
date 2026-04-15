package com.example.postgresql;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;


public class AuthDAO extends SupabaseDAO {


    public CompletableFuture<User> login(String username, String password) {


        String urlPath = String.format("/users?username=eq.%s&password=eq.%s",
                encodeUrlParameter(username),
                encodeUrlParameter(password));

        HttpRequest request = createBaseRequestBuilder(urlPath)
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            JsonNode root = objectMapper.readTree(response.body());


                            if (root.isArray() && root.size() > 0) {

                                JsonNode userNode = root.get(0);
                                return objectMapper.treeToValue(userNode, User.class);
                            } else {

                                return null;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    } else {
                        handleDaoError(response, "входа в систему");
                        return null;
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Сетевая ошибка при входе: " + e.getMessage());
                    return null;
                });
    }


    public CompletableFuture<Boolean> register(String username, String password) {


        ObjectNode jsonBody = objectMapper.createObjectNode()
                .put("username", username)
                .put("password", password)
                .put("role", "user");

        HttpRequest request = createBaseRequestBuilder("/users")
                .header("Prefer", "return=representation")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString()))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {

                    if (response.statusCode() == 201) {
                        return true;
                    } else if (response.statusCode() == 409) {

                        return false;
                    } else {
                        handleDaoError(response, "регистрации");
                        return false;
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Сетевая ошибка при регистрации: " + e.getMessage());
                    return false;
                });
    }
}