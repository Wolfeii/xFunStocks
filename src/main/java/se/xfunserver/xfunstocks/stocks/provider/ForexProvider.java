package se.xfunserver.xfunstocks.stocks.provider;

import pl.zankowski.iextrading4j.api.forex.ExchangeRate;
import pl.zankowski.iextrading4j.api.refdata.v1.Pair;
import pl.zankowski.iextrading4j.client.rest.request.forex.ExchangeRateRequestBuilder;
import se.xfunserver.xfunstocks.config.Settings;
import se.xfunserver.xfunstocks.stocks.common.CacheableProvider;

import java.math.BigDecimal;

public class ForexProvider extends CacheableProvider<ExchangeRate> {

    public static final String USD = "USD";

    public ForexProvider(Settings settings) {
        super(settings);
    }

    public BigDecimal getExchangeRate(String targetCurrency) {
        ExchangeRate exchangeRate = get(targetCurrency);
        if (exchangeRate == null) {
            return null;
        }

        return exchangeRate.getRate();
    }

    @Override
    protected ExchangeRate fetch(String key) {
        try {
            return getSettings().getCloudClient().executeRequest(new ExchangeRateRequestBuilder()
                    .withPair(new Pair(USD, key))
                    .build());
        } catch (Exception e) {
            return null;
        }
    }
}
