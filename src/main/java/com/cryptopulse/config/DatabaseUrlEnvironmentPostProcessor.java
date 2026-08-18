package com.cryptopulse.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String url = environment.getProperty("SPRING_DATASOURCE_URL");
        if (url != null && !url.isBlank() && !url.startsWith("jdbc:")) {
            String fixedUrl = "jdbc:" + url;
            Map<String, Object> map = new HashMap<>();
            map.put("SPRING_DATASOURCE_URL", fixedUrl);
            map.put("spring.datasource.url", fixedUrl);
            environment.getPropertySources().addFirst(new MapPropertySource("renderDatabaseUrlFix", map));
        }
    }
}
