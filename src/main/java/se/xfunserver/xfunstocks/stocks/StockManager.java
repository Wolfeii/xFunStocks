package se.xfunserver.xfunstocks.stocks;

import org.bukkit.entity.Player;
import pl.zankowski.iextrading4j.api.stocks.Quote;
import se.xfunserver.xfunstocks.config.Settings;
import se.xfunserver.xfunstocks.stocks.provider.ForexProvider;
import se.xfunserver.xfunstocks.stocks.provider.StockProvider;

import java.math.BigDecimal;

public class StockManager {

    private final Settings settings;

    private final ForexProvider forexProvider;
    private final StockProvider stockProvider;

    public StockManager(Settings settings) {
        this.settings = settings;

        this.forexProvider = new ForexProvider(settings);
        this.stockProvider = new StockProvider(settings);
    }

    public void cacheStocks(String... symbols) {
        stockProvider.get(symbols);
    }

    public Quote getStock(String symbol) {
        return stockProvider.get(symbol);
    }

    public BigDecimal getServerPrice(String symbol) {
        Quote quote = getStock(symbol);
        if (quote == null) {
            return null;
        }

        return getServerPrice(quote);
    }

    public BigDecimal getServerPrice(Quote quote) {
        BigDecimal price = quote.getLatestPrice().multiply(settings.getPriceMultiplier());
        if (quote.getCurrency().equalsIgnoreCase(ForexProvider.USD)) {
            return price;
        }

        BigDecimal conversionFactor = forexProvider.getExchangeRate(quote.getCurrency());
        if (conversionFactor == null) {
            return null;
        }

        return price.multiply(conversionFactor);
    }

    public boolean canNotUseStock(Player player, Quote quote) {
        return false;
    }

}
