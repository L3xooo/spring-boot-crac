package com.example.cracdemo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface SavedMessageRepository extends MongoRepository<SavedMessage, String> {
}

