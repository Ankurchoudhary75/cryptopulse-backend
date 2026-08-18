package com.cryptopulse.api;

import com.cryptopulse.event.AnomalyEventListener;
import com.cryptopulse.model.MarketAnomaly;
import com.cryptopulse.repository.MarketAnomalyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MarketAnomalyController.class)
class MarketAnomalyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MarketAnomalyRepository anomalyRepository;

    @MockBean
    private AnomalyEventListener sseEventListener;

    @Test
    void getLatestAnomalies_returnsAnomalyList() throws Exception {
        MarketAnomaly anomaly = new MarketAnomaly(
                "SOL", MarketAnomaly.AnomalyType.SURGE, new BigDecimal("190.00"),
                new BigDecimal("14.50"), new BigDecimal("5.00"), Instant.now(),
                MarketAnomaly.Severity.HIGH, "coingecko"
        );
        when(anomalyRepository.findTop20ByOrderByDetectedAtDesc()).thenReturn(List.of(anomaly));

        mockMvc.perform(get("/api/v1/anomalies/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("SOL"))
                .andExpect(jsonPath("$[0].anomalyType").value("SURGE"));
    }

    @Test
    void streamAnomalies_returnsTextEventStream() throws Exception {
        when(sseEventListener.registerEmitter()).thenReturn(new SseEmitter());

        mockMvc.perform(get("/api/v1/anomalies/stream"))
                .andExpect(status().isOk());
    }
}
