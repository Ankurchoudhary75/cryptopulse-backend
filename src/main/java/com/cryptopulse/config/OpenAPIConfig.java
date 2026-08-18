package com.cryptopulse.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CryptoPulse Intelligence Engine API")
                        .version("1.0.0")
                        .description("Multi-source resilient crypto & DeFi market ticker ingestion, real-time anomaly detection engine, SSE stream, and analytical REST service.")
                        .contact(new Contact()
                                .name("CryptoPulse Engineering")
                                .email("engineering@cryptopulse.io"))
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
