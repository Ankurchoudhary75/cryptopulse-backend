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
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
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

        String username = env.getProperty("SPRING_DATASOURCE_USERNAME");
        String password = env.getProperty("SPRING_DATASOURCE_PASSWORD");

        if (url != null && !url.isBlank()) {
            try {
                String uriStr = url.startsWith("jdbc:") ? url.substring(5) : url;
                if (uriStr.startsWith("postgres://") || uriStr.startsWith("postgresql://")) {
                    URI uri = new URI(uriStr);
                    String host = uri.getHost();
                    int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                    String path = uri.getPath() != null ? uri.getPath() : "/cryptopulse";
                    url = "jdbc:postgresql://" + host + ":" + port + path;

                    if (uri.getUserInfo() != null) {
                        String[] userInfo = uri.getUserInfo().split(":", 2);
                        if (username == null || username.isBlank()) username = userInfo[0];
                        if ((password == null || password.isBlank()) && userInfo.length > 1) password = userInfo[1];
                    }
                }
            } catch (Exception ignored) {}
            properties.setUrl(url);
        }

        properties.setDriverClassName("org.postgresql.Driver");
        if (username != null && !username.isBlank()) properties.setUsername(username);
        if (password != null && !password.isBlank()) properties.setPassword(password);

        return properties;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(properties.getConnectionTimeoutMs()))
                .setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                .additionalInterceptors((request, body, execution) -> {
                    request.getHeaders().set(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
                    request.getHeaders().set(HttpHeaders.ACCEPT, "application/json, text/plain, */*");
                    request.getHeaders().set(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.9");
                    return execution.execute(request, body);
                })
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
