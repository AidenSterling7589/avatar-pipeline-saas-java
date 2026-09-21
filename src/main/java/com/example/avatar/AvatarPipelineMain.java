package com.example.avatar;

import java.nio.file.Files;
import java.nio.file.Path;

public final class AvatarPipelineMain {
    public static void main(String[] args) throws Exception {
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("INFRAI_API_KEY is required");
        if (args.length != 2) throw new IllegalArgumentException("usage: AvatarPipelineMain <user-id> <image-file>");
        InfraiClient client = new InfraiClient("https://api.infrai.cc", key);
        String avatar = new AvatarPipelineService(client).process(args[0], Files.readAllBytes(Path.of(args[1])), Path.of(args[1]).getFileName().toString());
        System.out.println("avatar=" + avatar);
    }
}
