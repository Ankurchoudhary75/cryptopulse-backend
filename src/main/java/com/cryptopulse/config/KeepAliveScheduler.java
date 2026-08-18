package com.cryptopulse.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class KeepAliveScheduler {

    private static final Logger log = LoggerFactory.getLogger(KeepAliveScheduler.class);

    private final RestTemplate restTemplate;

    @Value("${cryptopulse.live-url:https://cryptopulse-backend-o2zp.onrender.com/actuator/health}")
    private String liveUrl;

    public KeepAliveScheduler(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Executes every 10 minutes (600,000 ms) to send an outbound HTTPS ping 
     * through Render's public edge gateway back to this instance.
     * This registers inbound HTTP traffic on Render, preventing container sleep.
     */
    @Scheduled(fixedRate = 600000, initialDelay = 60000)
    public void sendKeepAlivePing() {
        try {
            log.info("Sending outbound public keep-alive ping to Render gateway: {}", liveUrl);
            restTemplate.getForObject(liveUrl, String.class);
            log.info("Keep-alive ping acknowledged cleanly");
        } catch (Exception e) {
            log.warn("Keep-alive ping failed (non-critical): {}", e.getMessage());
        }
    }
}
