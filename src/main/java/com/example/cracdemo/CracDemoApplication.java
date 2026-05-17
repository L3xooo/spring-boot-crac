package com.example.cracdemo;

import jakarta.annotation.PostConstruct;
import org.crac.Context;
import org.crac.Core;
import org.crac.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;

@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class
})
public class CracDemoApplication implements Resource {
    private static final Logger log = LoggerFactory.getLogger(CracDemoApplication.class);

    @Value("${env:Hello from application.yml}")
    private String env;

    public CracDemoApplication() {
        Core.getGlobalContext().register(this);
    }

    @PostConstruct
    public void init() {
        log.info("Application initialized at {} with env='{}'", System.currentTimeMillis(), env);
    }

    @Override
    public void beforeCheckpoint(Context<? extends Resource> context) {
        log.info("Before checkpoint at {}", System.currentTimeMillis());
    }

    @Override
    public void afterRestore(Context<? extends Resource> context) {
        log.info("After restore at {}", System.currentTimeMillis());
    }

    public static void main(String[] args) {
        SpringApplication.run(CracDemoApplication.class, args);
    }
}
