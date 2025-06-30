package com.gameadvisor.client.network;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.util.ArrayList;
import java.util.List;
import com.gameadvisor.client.model.Game;

public class ApiClient {
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private static final String BASE_URL = "http://localhost:8080/api/games";

    public List<Game> getGames() throws Exception {
        Request request = new Request.Builder().url(BASE_URL).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("Failed to fetch game list: " + response);
                return new ArrayList<>();
            }
            return mapper.readValue(response.body().string(), new TypeReference<List<Game>>() {});
        }
    }
} 