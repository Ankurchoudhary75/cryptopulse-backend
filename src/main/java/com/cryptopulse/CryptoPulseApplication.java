package com.cryptopulse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class CryptoPulseApplication {

    static {
        String dbUrl = System.getenv("SPRING_DATASOURCE_URL");
        if (dbUrl != null && !dbUrl.isBlank() && !dbUrl.startsWith("jdbc:")) {
            System.setProperty("SPRING_DATASOURCE_URL", "jdbc:" + dbUrl);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(CryptoPulseApplication.class, args);
    }
}
