package com.example.cracdemo;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.crac.Context;
import org.crac.Core;
import org.crac.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class HikariCracResource implements Resource {

    private static final Logger log = LoggerFactory.getLogger(HikariCracResource.class);

    private final HikariDataSource hikari;

    public HikariCracResource(DataSource dataSource) {
        if (!(dataSource instanceof HikariDataSource h)) {
            throw new IllegalStateException(
                "Expected HikariDataSource, got: " + dataSource.getClass().getName()
            );
        }

        this.hikari = h;
        Core.getGlobalContext().register(this);

        log.info("HikariCracResource registered");
    }

    @Override
    public void beforeCheckpoint(Context<? extends Resource> context) throws Exception {
        log.info("CRaC beforeCheckpoint – suspending and evicting Hikari/Postgres connections");

        HikariPoolMXBean pool = hikari.getHikariPoolMXBean();

        if (pool == null) {
            log.warn("HikariPoolMXBean is null, skipping Hikari CRaC handling");
            return;
        }

        log.info("Hikari before checkpoint: active={}, idle={}, total={}, waiting={}",
            pool.getActiveConnections(),
            pool.getIdleConnections(),
            pool.getTotalConnections(),
            pool.getThreadsAwaitingConnection()
        );

        pool.suspendPool();
        pool.softEvictConnections();

        long deadline = System.currentTimeMillis() + 5000;

        while (System.currentTimeMillis() < deadline) {
            int active = pool.getActiveConnections();
            int idle = pool.getIdleConnections();
            int total = pool.getTotalConnections();

            if (active == 0 && idle == 0 && total == 0) {
                log.info("Hikari ready for checkpoint: active=0, idle=0, total=0");
                return;
            }

            log.info("Waiting for Hikari connections to close: active={}, idle={}, total={}",
                active, idle, total
            );

            Thread.sleep(100);
        }

        throw new IllegalStateException(
            "Hikari still has open connections before checkpoint: active="
                + pool.getActiveConnections()
                + ", idle=" + pool.getIdleConnections()
                + ", total=" + pool.getTotalConnections()
        );
    }

    @Override
    public void afterRestore(Context<? extends Resource> context) {
        log.info("CRaC afterRestore – resuming Hikari/Postgres pool");

        HikariPoolMXBean pool = hikari.getHikariPoolMXBean();

        if (pool == null) {
            log.warn("HikariPoolMXBean is null after restore");
            return;
        }

        pool.resumePool();

        log.info("Hikari resumed after restore. New Postgres connections will be opened on demand");
    }
}