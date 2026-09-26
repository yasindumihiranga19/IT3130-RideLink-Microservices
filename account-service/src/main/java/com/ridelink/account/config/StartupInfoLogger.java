package com.ridelink.account.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StartupInfoLogger {

    private final Environment env;

    public StartupInfoLogger(Environment env) {
        this.env = env;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logStartupInfo() {
        String name = env.getProperty("spring.application.name");
        String port = env.getProperty("server.port");
        String dbUrl = env.getProperty("spring.datasource.url", "");
        String dbName = dbUrl.substring(dbUrl.lastIndexOf('/') + 1);

        log.info("""

                ------------------------------------------------------
                  {} is running
                  Local:    http://localhost:{}
                  Swagger:  http://localhost:{}/swagger-ui/index.html
                  Database: {}
                ------------------------------------------------------
                """, name, port, port, dbName);
    }
}