package com.example.avatar;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

public final class InfraiClient {
    private final HttpClient http = HttpClient.newHttpClient();
    private final String baseUrl;
    private final String key;

    public InfraiClient(String baseUrl, String key) {
        this.baseUrl = baseUrl;
        this.key = key;
    }

    public String post(String path, String json) throws IOException, InterruptedException {
        return send("POST", path, json);
    }

    public String patch(String path, String json) throws IOException, InterruptedException {
        return send("PATCH", path, json);
    }

    private String send(String method, String path, String json) throws IOException, InterruptedException {
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(30)).header("Authorization", "Bearer " + key)
                    .header("Content-Type", "application/json").header("Idempotency-Key", "avatar-pipeline-v1")
                    .method(method, HttpRequest.BodyPublishers.ofString(json)).build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            if (body.contains("\"ok\":false")) throw new IOException("Infrai rejected request: " + body);
            if (response.statusCode() == 429) {
                Thread.sleep((long) Math.pow(2, attempt) * 250L);
                continue;
            }
            if (response.statusCode() >= 500) throw new IOException("Infrai transport failure: " + response.statusCode());
            return body;
        }
        throw new IOException("Infrai request retry budget exhausted");
    }

    public static String asDataUri(byte[] bytes, String mime) {
        return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(bytes);
    }
}
