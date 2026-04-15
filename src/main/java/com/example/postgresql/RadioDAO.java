package com.example.postgresql;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RadioDAO extends SupabaseDAO {

    public CompletableFuture<List<RadioStation>> getAllStations() {
        HttpRequest request = createBaseRequestBuilder("/radio_stations?order=name.asc").GET().build();


        final List<RadioStation> EMPTY_RADIO_LIST = Collections.emptyList();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            return objectMapper.readValue(response.body(), new TypeReference<List<RadioStation>>() {});
                        } catch (IOException e) {
                            e.printStackTrace();

                            return EMPTY_RADIO_LIST;
                        }
                    } else {
                        handleDaoError(response, "получении списка радиостанций");

                        return EMPTY_RADIO_LIST;
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Сетевая ошибка при получении радиостанций: " + e.getMessage());

                    return EMPTY_RADIO_LIST;
                });
    }
    public CompletableFuture<Boolean> addStation(
            String name,
            String streamUrl,
            String coverUrl
    ) {
        ObjectNode jsonBody = objectMapper.createObjectNode()
                .put("name", name)
                .put("stream_url", streamUrl)
                .put("cover_url", coverUrl);

        return sendPostRequest(
                "/radio_stations",
                jsonBody,
                "добавлении радиостанции: " + name
        );
    }

    public CompletableFuture<Boolean> deleteStation(int stationId) {
        String pathQuery = "/radio_stations?id=eq." + stationId;
        return sendDeleteRequest(pathQuery, "удалении радиостанции с ID " + stationId);
    }

}