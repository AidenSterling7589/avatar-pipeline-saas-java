package com.example.avatar;

public final class AvatarPipelineTest {
    public static void main(String[] args) {
        String envelope = "{\"ok\":true,\"data\":{\"image\":\"stored-avatar\"},\"error\":null,\"metadata\":{}}";
        if (!"stored-avatar".equals(AvatarPipelineService.imageValue(envelope))) throw new AssertionError("image extraction");
        if (!"raw".equals(AvatarPipelineService.imageValue("raw"))) throw new AssertionError("fallback extraction");
        System.out.println("AvatarPipelineTest passed: stored image is selected for the user record");
    }
}
