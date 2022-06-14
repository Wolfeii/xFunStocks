package se.xfunserver.xfunstocks.inventories.utils.common;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import pl.zankowski.iextrading4j.api.stocks.Quote;
import se.xfunserver.xfunstocks.config.Messages;
import se.xfunserver.xfunstocks.config.Settings;
import se.xfunserver.xfunstocks.config.common.ConfigSection;
import se.xfunserver.xfunstocks.stocks.StockManager;
import se.xfunserver.xfunstocks.xFunStocks;

import java.math.BigDecimal;
import java.util.*;

public abstract class StockDataInventory extends StockInventory {

    protected final xFunStocks plugin;
    protected final StockManager stockManager;
    protected final Messages messages;
    protected final Settings settings;
    protected final ConfigSection section;

    protected final String name;

    protected StockDataInventory(
            xFunStocks plugin,
            StockManager stockManager,
            Messages messages,
            Settings settings,
            ConfigSection section) {
        super(plugin);

        this.plugin = plugin;
        this.stockManager = stockManager;
        this.messages = messages;
        this.settings = settings;
        this.section = section;

        this.name = section.getString("name");
    }

    protected abstract Inventory buildInventory(List<Map.Entry<Quote, BigDecimal>> stocks);

    public void openInventory(Player player, String... symbols) {
        Bukkit.getScheduler()
                .runTaskAsynchronously(
                        plugin,
                        () -> {
                            Map<Quote, BigDecimal> stockMap = lookupStocks(player, symbols);

                            List<Map.Entry<Quote, BigDecimal>> stocks = new ArrayList<>(stockMap.entrySet());
                            Bukkit.getScheduler()
                                    .runTask(
                                            plugin,
                                            () -> {
                                                Inventory inventory = buildInventory(stocks);

                                                player.openInventory(inventory);
                                                addViewer(player);
                                            });
                        });
    }

    private Map<Quote, BigDecimal> lookupStocks(Player player, String... symbols) {
        Map<Quote, BigDecimal> stockMap = new LinkedHashMap<>();
        for (String symbol : symbols) {
            Quote quote = stockManager.getStock(symbol);
            if (stockManager.canNotUseStock(player, quote)) {
                return new LinkedHashMap<>();
            }

            BigDecimal price = stockManager.getServerPrice(quote);
            if (price == null) {
                messages.sendInvalidStock(player);
                return new LinkedHashMap<>();
            }

            stockMap.put(quote, price);
        }

        return stockMap;
    }

    protected Map<String, Object> buildStockDataMap(Quote quote, BigDecimal serverPrice) {
        return Map.ofEntries(
                Map.entry("<name>", quote.getCompanyName()),
                Map.entry("<exchange>", quote.getPrimaryExchange()),
                Map.entry("<cap>", settings.formatSigFig(quote.getMarketCap())),
                Map.entry("<market-price>", settings.format(quote.getLatestPrice())),
                Map.entry("<market-currency>", quote.getCurrency()),
                Map.entry("<server-price>", settings.format(serverPrice)),
                Map.entry("<change-close>", settings.format(quote.getChange())),
                Map.entry("<change-year-high>", settings.format(quote.getWeek52Low())),
                Map.entry("<symbol>", quote.getSymbol().toUpperCase()),
                Map.entry("<open-price>", settings.format(quote.getOpen())),
                Map.entry("<volume>", settings.formatSigFig(quote.getVolume())),
                Map.entry("<close-price>", settings.format(quote.getPreviousClose())),
                Map.entry("<year-high>", settings.format(quote.getWeek52High())),
                Map.entry("<year-low>", settings.format(quote.getWeek52Low()))
        );
    }
}
