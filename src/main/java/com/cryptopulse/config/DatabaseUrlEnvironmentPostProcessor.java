package com.cryptopulse.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String url = environment.getProperty("SPRING_DATASOURCE_URL");
        if (url == null || url.isBlank()) {
            url = environment.getProperty("spring.datasource.url");
        }

        if (url != null && !url.isBlank()) {
            try {
                String uriStr = url;
                if (uriStr.startsWith("jdbc:")) {
                    uriStr = uriStr.substring(5);
                }

                if (uriStr.startsWith("postgres://") || uriStr.startsWith("postgresql://")) {
                    URI uri = new URI(uriStr);
                    String host = uri.getHost();
                    int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                    String path = uri.getPath() != null ? uri.getPath() : "/cryptopulse";

                    String cleanJdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;

                    Map<String, Object> map = new HashMap<>();
                    map.put("SPRING_DATASOURCE_URL", cleanJdbcUrl);
                    map.put("spring.datasource.url", cleanJdbcUrl);

                    if (uri.getUserInfo() != null) {
                        String[] userInfo = uri.getUserInfo().split(":", 2);
                        map.put("SPRING_DATASOURCE_USERNAME", userInfo[0]);
                        map.put("spring.datasource.username", userInfo[0]);
                        if (userInfo.length > 1) {
                            map.put("SPRING_DATASOURCE_PASSWORD", userInfo[1]);
                            map.put("spring.datasource.password", userInfo[1]);
                        }
                    }

                    environment.getPropertySources().addFirst(new MapPropertySource("renderDatabaseUrlFix", map));
                }
            } catch (Exception ignored) {
                // Keep original settings if URI parsing fails
            }
        }
    }
}
