package com.example.postgresql;

import com.fasterxml.jackson.core.type.TypeReference;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AdminDAO extends SupabaseDAO {

    private static final String USERS_TABLE = "/users";

    public CompletableFuture<List<User>> getAllUsers() {

        HttpRequest request = createBaseRequestBuilder(USERS_TABLE + "?order=username.asc").GET().build();
        return sendAndDeserializeList(
                request,
                new TypeReference<List<User>>() {},
                "получении списка пользователей"
        );
    }

    public CompletableFuture<Boolean> deleteUser(int userId) {
        String pathQuery = USERS_TABLE + "?id=eq." + userId;
        String operation = "удалении пользователя с ID " + userId;

        return sendDeleteRequest(pathQuery, operation);
    }
}