package com.example.cracdemo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.Document;
import org.crac.Context;
import org.crac.Core;
import org.crac.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.mongodb.autoconfigure.MongoProperties;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * CRaC resource that closes the MongoDB client before a checkpoint and
 * re-establishes the connection after restore.
 *
 * <p>Required JVM flags (add to your checkpoint/restore launch config):
 * <pre>
 *   --add-opens java.base/java.lang.reflect=ALL-UNNAMED
 * </pre>
 * This is needed so that {@link Field#(true)} can reach the
 * {@code private final mongoClient} field inside
 * {@link SimpleMongoClientDatabaseFactory} (or its superclass).
 */
@Component
public class DatabaseCracResource implements Resource {

    private static final Logger log = LoggerFactory.getLogger(DatabaseCracResource.class);

    private MongoClient mongoClient;
    private final MongoDatabaseFactory mongoDatabaseFactory;
    private final MongoProperties mongoProperties;

    public DatabaseCracResource(MongoClient mongoClient,
                                MongoDatabaseFactory mongoDatabaseFactory,
                                MongoProperties mongoProperties) {
        this.mongoClient = mongoClient;
        this.mongoDatabaseFactory = mongoDatabaseFactory;
        this.mongoProperties = mongoProperties;
        Core.getGlobalContext().register(this);
    }

    @Override
    public void beforeCheckpoint(Context<? extends Resource> context) throws Exception {
        log.info("CRaC beforeCheckpoint – closing MongoDB client");
        mongoClient.close();
        log.info("MongoDB client closed");
    }

    @Override
    public void afterRestore(Context<? extends Resource> context) throws Exception {
        log.info("CRaC afterRestore – re-establishing MongoDB connection");
        try {
            // 1. Create a brand-new MongoClient from the original URI.
            String uri = mongoProperties.determineUri();
            mongoClient = MongoClients.create(uri);
            log.info("New MongoClient created for URI: {}", uri);

            // 2. Inject the new client into the existing factory so that every
            //    Spring bean that holds a reference to MongoDatabaseFactory
            //    (e.g. MongoTemplate) picks up the fresh connection automatically.
            injectClientIntoFactory(mongoClient);

            // 3. Verify the connection before the application starts serving traffic.
            mongoDatabaseFactory.getMongoDatabase().runCommand(new Document("ping", 1));
            log.info("MongoDB connection verified – ready for operations");

        } catch (Exception e) {
            log.error("Failed to restore MongoDB connection after CRaC restore", e);
            throw new RuntimeException("MongoDB connection restoration failed", e);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Injects {@code newClient} into the factory's {@code mongoClient} field.
     *
     * <p>The field is declared in {@link SimpleMongoClientDatabaseFactory} or one
     * of its superclasses, so we walk the hierarchy until we find it.
     */
    private void injectClientIntoFactory(MongoClient newClient) throws Exception {
        Field field = findFieldInHierarchy(mongoDatabaseFactory.getClass(), "mongoClient");
        if (field == null) {
            throw new IllegalStateException(
                "Could not locate 'mongoClient' field in " +
                    mongoDatabaseFactory.getClass().getName() +
                    " or any of its superclasses. " +
                    "Verify your Spring Data MongoDB version and field name.");
        }

        field.setAccessible(true);   // requires --add-opens java.base/java.lang.reflect=ALL-UNNAMED
        field.set(mongoDatabaseFactory, newClient);
        log.info("Injected new MongoClient into {}", mongoDatabaseFactory.getClass().getSimpleName());
    }

    /**
     * Walks the class hierarchy (excluding {@link Object}) looking for a field
     * with the given name. Returns {@code null} if no match is found.
     */
    private static Field findFieldInHierarchy(Class<?> clazz, String fieldName) {
        while (clazz != null && clazz != Object.class) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
}