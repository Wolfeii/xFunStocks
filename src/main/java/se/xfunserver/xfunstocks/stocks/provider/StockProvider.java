package se.xfunserver.xfunstocks.stocks.provider;

import pl.zankowski.iextrading4j.api.stocks.Quote;
import pl.zankowski.iextrading4j.client.rest.request.stocks.QuoteRequestBuilder;
import se.xfunserver.xfunstocks.config.Settings;
import se.xfunserver.xfunstocks.stocks.common.CacheableProvider;

import java.util.HashMap;
import java.util.Map;

public class StockProvider extends CacheableProvider<Quote> {

    public StockProvider(Settings settings) {
        super(settings);
    }

    @Override
    protected Quote fetch(String key) {
        try {
            return getSettings().getCloudClient().executeRequest(new QuoteRequestBuilder()
                    .withSymbol(key)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected Map<String, Quote> fetch(String[] keys) {
        try {
            Map<String, Quote> result = new HashMap<>();
            for (String key : keys) {
                result.put(key, fetch(key));
            }

            return result;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}
