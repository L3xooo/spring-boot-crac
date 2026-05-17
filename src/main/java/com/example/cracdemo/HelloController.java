package com.example.cracdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
public class HelloController {

    @Autowired
    private SavedMessageRepository repository;

    @Autowired
    private PostgresMessageRepository postgresMessageRepository;

    @Value("${env:Hello from application.yml}")
    private String env;

    @GetMapping("/hello")
    public String hello() {
        return "Caught You! env='" + env + "'";
    }

    @PostMapping("/mongo/save")
    public SavedMessage saveMongo() {
        SavedMessage savedMessage = new SavedMessage(null, "message", Instant.now());
        return repository.save(savedMessage);
    }

    @PostMapping("/postgres/save")
    public PostgresMessage savePostgres() {
        PostgresMessage message = new PostgresMessage(null, "message", Instant.now());
        return postgresMessageRepository.save(message);
    }
}
