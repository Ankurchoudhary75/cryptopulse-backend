package com.cryptopulse.event;

import com.cryptopulse.model.MarketAnomaly;
import org.springframework.context.ApplicationEvent;

public class MarketAnomalyEvent extends ApplicationEvent {

    private final MarketAnomaly anomaly;

    public MarketAnomalyEvent(Object source, MarketAnomaly anomaly) {
        super(source);
        this.anomaly = anomaly;
    }

    public MarketAnomaly getAnomaly() {
        return anomaly;
    }
}
