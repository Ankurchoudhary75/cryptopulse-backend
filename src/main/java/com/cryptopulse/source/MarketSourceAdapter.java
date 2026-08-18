package com.cryptopulse.source;

import java.util.List;

public interface MarketSourceAdapter {

    String getProviderName();

    int getPriority();

    List<RawTickerData> fetchTickers() throws SourceFetchException;

    boolean checkHealth();
}
