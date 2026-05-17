package com.example.cracdemo;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.repository.MongoRepository;

@Lazy
public interface SavedMessageRepository extends MongoRepository<SavedMessage, String> {
}

