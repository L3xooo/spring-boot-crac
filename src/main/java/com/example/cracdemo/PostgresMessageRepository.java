package com.example.cracdemo;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.JpaRepository;

@Lazy
public interface PostgresMessageRepository extends JpaRepository<PostgresMessage, Long> {
}

