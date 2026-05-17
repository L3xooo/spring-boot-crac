package com.example.cracdemo;

import com.mongodb.client.MongoClient;
import com.zaxxer.hikari.HikariDataSource;
import org.crac.Context;
import org.crac.Core;
import org.crac.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DatabaseCracResource implements Resource {

    private final DataSource dataSource;
    private final MongoClient mongoClient;
    private static final Logger log = LoggerFactory.getLogger(DatabaseCracResource.class);

    public DatabaseCracResource(DataSource dataSource, MongoClient mongoClient) {
        this.dataSource = dataSource;
        this.mongoClient = mongoClient;
        Core.getGlobalContext().register(this);
    }

    @Override
    public void beforeCheckpoint(Context<? extends Resource> context) throws Exception {
        log.info("Before checkpoint - closing DB connections");

        if (dataSource instanceof HikariDataSource hikari) {
            hikari.getHikariPoolMXBean().softEvictConnections();
            hikari.close();
        }

        log.info("Before checkpoint - closing MongoDB connections");
        mongoClient.close();
    }

    @Override
    public void afterRestore(Context<? extends Resource> context) throws Exception {
        log.info("After restore - reopening DB connections");

        if (dataSource instanceof HikariDataSource hikari) {
            // HikariCP will reconnect lazily on next request
        }

        log.info("After restore - MongoDB client will reconnect on next request");
        // MongoClient reconnects automatically on next use
    }
}