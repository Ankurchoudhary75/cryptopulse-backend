package com.cryptopulse.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.concurrent.Executor;

@Configuration
public class AppConfig {

    private final IngestionProperties properties;

    public AppConfig(IngestionProperties properties) {
        this.properties = properties;
    }

    @Bean
    @Primary
    public DataSourceProperties dataSourceProperties(Environment env) {
        DataSourceProperties properties = new DataSourceProperties();
        String url = env.getProperty("SPRING_DATASOURCE_URL");
        if (url == null || url.isBlank()) {
            url = env.getProperty("spring.datasource.url");
        }
        if (url != null && !url.isBlank() && !url.startsWith("jdbc:")) {
            url = "jdbc:" + url;
        }
        if (url != null && !url.isBlank()) {
            properties.setUrl(url);
        }
        properties.setDriverClassName("org.postgresql.Driver");

        String username = env.getProperty("SPRING_DATASOURCE_USERNAME");
        if (username != null && !username.isBlank()) {
            properties.setUsername(username);
        }
        String password = env.getProperty("SPRING_DATASOURCE_PASSWORD");
        if (password != null && !password.isBlank()) {
            properties.setPassword(password);
        }

        return properties;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(properties.getConnectionTimeoutMs()))
                .setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("CryptoPulseAsync-");
        executor.initialize();
        return executor;
    }
}
