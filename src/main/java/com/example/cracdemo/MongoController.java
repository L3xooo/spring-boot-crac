package com.example.cracdemo;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class MongoController {
    private final SavedMessageRepository savedMessageRepository;

    public MongoController(SavedMessageRepository savedMessageRepository) {
        this.savedMessageRepository = savedMessageRepository;
    }

    @PostMapping("/mongo/save")
    public Map<String, Object> save(@RequestBody SaveMessageRequest request) {
        SavedMessage savedMessage = new SavedMessage(null, request.message(), Instant.now());
        SavedMessage saved = savedMessageRepository.save(savedMessage);
        return Map.of(
                "status", "saved",
                "id", saved.getId(),
                "message", saved.getMessage(),
                "createdAt", saved.getCreatedAt().toString()
        );
    }
}

record SaveMessageRequest(String message) {
}
