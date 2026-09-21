package com.example.avatar;

import java.io.IOException;

public final class AvatarPipelineService {
    private final InfraiClient client;

    public AvatarPipelineService(InfraiClient client) { this.client = client; }

    public String process(String userId, byte[] imageBytes, String filename) throws IOException, InterruptedException {
        String image = client.post("/v1/image/upload", "{\"file\":\"" + InfraiClient.asDataUri(imageBytes, "image/jpeg") + "\",\"filename\":\"" + filename + "\"}");
        String cropped = client.post("/v1/image/smart_crop", "{\"image\":\"" + imageValue(image) + "\",\"aspect\":\"1:1\"}");
        String resized = client.post("/v1/image/resize", "{\"image\":\"" + imageValue(cropped) + "\",\"width\":256,\"height\":256,\"fit\":\"cover\",\"enlarge\":false,\"format\":\"webp\",\"store\":true}");
        client.patch("/v1/auth/user/update/" + userId, "{\"user_id\":\"" + userId + "\",\"metadata\":{\"avatar\":\"" + imageValue(resized) + "\"},\"idempotency_key\":\"avatar-" + userId + "\"}");
        return imageValue(resized);
    }

    static String imageValue(String envelope) {
        int marker = envelope.indexOf("\"image\":\"");
        if (marker < 0) return envelope;
        int start = marker + 9;
        int end = envelope.indexOf('"', start);
        return end < 0 ? envelope.substring(start) : envelope.substring(start, end);
    }
}
