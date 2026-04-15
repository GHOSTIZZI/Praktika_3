package com.example.postgresql;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class SupabaseDAO {


    //public static final String SUPABASE_URL = "https://xxatkzdplbgnuibdykyd.supabase.co/rest/v1";
    public static final String SUPABASE_URL =  "https://drtawqvwgdtmljytomad.supabase.co/rest/v1";
    //private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inh4YXRremRwbGJnbnVpYmR5a3lkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjM0MTI0MjEsImV4cCI6MjA3ODk4ODQyMX0.svFtSKnqmarh3TiybOjJYQ_t0sZ_vUv8D9Q2QUcdxkk";
    private static final String SUPABASE_ANON_KEY = "sb_publishable_9QholS-khKzUoH4jyg2vrg_X0NXh8tT";
    protected final HttpClient httpClient;
    protected final ObjectMapper objectMapper;

    public SupabaseDAO() {
        this.httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).connectTimeout(Duration.ofSeconds(15)).build();
        this.objectMapper = new ObjectMapper();
    }


    protected HttpRequest.Builder createBaseRequestBuilder(String pathQuery) {
        String fullUrl = SUPABASE_URL + pathQuery;
        return createBaseRequestBuilder(URI.create(fullUrl));
    }


    protected HttpRequest.Builder createBaseRequestBuilder(URI uri) {
        return HttpRequest.newBuilder()
                .uri(uri)
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json");
    }


    protected String encodeUrlParameter(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        try {

            String encoded = URLEncoder.encode(value, StandardCharsets.UTF_8.toString());

            return encoded.replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            System.err.println("Ошибка кодирования URL-параметра: " + e.getMessage());
            return value;
        }
    }

    protected void handleDaoError(HttpResponse<String> response, String operation) {
        String errorBody = response.body();
        String message = String.format(
                "Ошибка при %s. Статус: %d. Ответ API: %s",
                operation,
                response.statusCode(),
                errorBody.length() > 200 ? errorBody.substring(0, 200) + "..." : errorBody
        );

        new RuntimeException(message).printStackTrace();
    }

    // =========================================================================
    // 2. УНИВЕРСАЛЬНЫЕ МЕТОДЫ GET (ПОЛУЧЕНИЕ СПИСКА)
    // =========================================================================

    protected <T> CompletableFuture<List<T>> sendAndDeserializeList(
            HttpRequest request,
            TypeReference<List<T>> typeRef,
            String operation) {

        final List<T> emptyList = Collections.emptyList();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            return objectMapper.readValue(response.body(), typeRef);
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.err.println("Ошибка десериализации при " + operation + ": " + e.getMessage());
                            return emptyList;
                        }
                    } else {
                        handleDaoError(response, operation);
                        return emptyList;
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Сетевая ошибка при " + operation + ": " + e.getMessage());
                    e.printStackTrace();
                    return emptyList;
                });
    }

    // =========================================================================
    // 3. УНИВЕРСАЛЬНЫЕ МЕТОДЫ МОДИФИКАЦИИ (POST, PATCH, DELETE)
    // =========================================================================

    protected CompletableFuture<Boolean> sendPostRequest(String path, ObjectNode jsonBody, String operation) {
        HttpRequest request = createBaseRequestBuilder(path)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString()))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 201) {
                        return true;
                    } else {
                        handleDaoError(response, operation);
                        return false;
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Сетевая ошибка при " + operation + ": " + e.getMessage());
                    e.printStackTrace();
                    return false;
                });
    }



    protected CompletableFuture<Boolean> sendDeleteRequest(String pathQuery, String operation) {
        HttpRequest request = createBaseRequestBuilder(pathQuery)
                .DELETE()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 204) {
                        return true;
                    } else {
                        handleDaoError(response, operation);
                        return false;
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Сетевая ошибка при " + operation + ": " + e.getMessage());
                    e.printStackTrace();
                    return false;
                });
    }
}