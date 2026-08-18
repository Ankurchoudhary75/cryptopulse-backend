package com.cryptopulse.pipeline;

import com.cryptopulse.model.MarketTicker;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MarketDataValidator {

    public boolean isValid(MarketTicker ticker) {
        if (ticker == null) {
            return false;
        }
        if (ticker.getSymbol() == null || ticker.getSymbol().isBlank() || "UNKNOWN".equals(ticker.getSymbol())) {
            return false;
        }
        if (ticker.getName() == null || ticker.getName().isBlank()) {
            return false;
        }
        if (ticker.getPriceUsd() == null || ticker.getPriceUsd().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (ticker.getSource() == null || ticker.getSource().isBlank()) {
            return false;
        }
        return true;
    }
}
