package com.cryptopulse.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class AnomalyEventListener {

    private static final Logger log = LoggerFactory.getLogger(AnomalyEventListener.class);
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter registerEmitter() {
        SseEmitter emitter = new SseEmitter(180_000L); // 3-minute timeout
        this.emitters.add(emitter);

        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> this.emitters.remove(emitter));
        emitter.onError((e) -> this.emitters.remove(emitter));

        log.info("New SSE client connected for Market Anomaly stream. Active clients: {}", emitters.size());
        return emitter;
    }

    @EventListener
    public void handleMarketAnomaly(MarketAnomalyEvent event) {
        log.info("Broadcasting MarketAnomalyEvent for symbol: {} [{}] to {} SSE clients",
                event.getAnomaly().getSymbol(), event.getAnomaly().getAnomalyType(), emitters.size());

        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("market-anomaly")
                        .data(event.getAnomaly()));
            } catch (IOException e) {
                log.warn("Failed to send SSE anomaly to client, removing emitter: {}", e.getMessage());
                deadEmitters.add(emitter);
            }
        }
        emitters.removeAll(deadEmitters);
    }
}
