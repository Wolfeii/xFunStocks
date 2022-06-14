package se.xfunserver.xfunstocks.inventories.portfolio;

import org.bukkit.inventory.ItemStack;
import pl.zankowski.iextrading4j.api.stocks.Quote;
import se.xfunserver.xfunstocks.config.Messages;
import se.xfunserver.xfunstocks.config.Settings;
import se.xfunserver.xfunstocks.config.common.ConfigSection;
import se.xfunserver.xfunstocks.inventories.utils.paged.PagedInventory;
import se.xfunserver.xfunstocks.player.PlayerManager;
import se.xfunserver.xfunstocks.player.player.StockPlayer;
import se.xfunserver.xfunstocks.player.player.data.StockData;
import se.xfunserver.xfunstocks.stocks.StockManager;
import se.xfunserver.xfunstocks.utils.Utils;
import se.xfunserver.xfunstocks.xFunStocks;

import java.math.BigDecimal;
import java.util.*;

public class PortfolioInventory extends PagedInventory<UUID, Quote, StockData> {

    private final StockManager stockManager;
    private final PlayerManager playerManager;

    public PortfolioInventory(xFunStocks plugin, Messages messages, PlayerManager playerManager,
          StockManager stockManager, Settings settings, ConfigSection section) {
        super(plugin, messages, settings, section);

        this.stockManager = stockManager;
        this.playerManager = playerManager;
    }


    @Override
    protected Map<Quote, StockData> getContent(UUID lookup) {
        Map<String, StockData> data = getData(lookup);
        Map<Quote, StockData> stockDataMap = new TreeMap<>(new StockComparator());
        for (Map.Entry<String, StockData> entry : data.entrySet()) {
            if (entry.getValue().getQuantity() == 0) {
                continue;
            }

            stockDataMap.put(stockManager.getStock(entry.getKey()), entry.getValue());
        }

        return stockDataMap;
    }

    @Override
    protected Map<String, Object> getExtraData(UUID lookup) {
        Map<String, Object> dataMap = new HashMap<>();
        StockPlayer stockPlayer = playerManager.forceGetStockPlayer(lookup);
        if (stockPlayer == null) {
            return dataMap;
        }

        BigDecimal currentValue = playerManager.getCurrentValue(stockPlayer);
        dataMap.put("purchase_value", stockPlayer.getPortfolioValue());
        dataMap.put("current_value", currentValue);
        dataMap.put("net_value", stockPlayer.getProfitMargin(currentValue));
        return dataMap;
    }

    @Override
    protected ItemStack getContentStack(ItemStack baseStack, Quote key, StockData value) {
        BigDecimal currentPrice = stockManager.getServerPrice(key);
        if (currentPrice == null) {
            return baseStack;
        }

        currentPrice = currentPrice.multiply(BigDecimal.valueOf(value.getQuantity()));
        BigDecimal net = currentPrice.subtract(value.getValue());
        return Utils.updateItemStack(
                baseStack,
                Map.of(
                        "<symbol>", key.getSymbol().toUpperCase(),
                        "<name>", key.getCompanyName(),
                        "<quantity>", value.getQuantity(),
                        "<current-value>", settings.format(currentPrice),
                        "<purchase-value>", settings.format(value.getValue()),
                        "<net>", settings.format(net),
                        "<server-currency>", plugin.getEconomy().currencyNamePlural()
                )
        );
    }

    @Override
    protected ItemStack getExtraItem(ItemStack baseStack, Map<String, Object> extraData) {
        return Utils.updateItemStack(
                baseStack,
                Map.of(
                        "<purchase-value>", settings.format(((BigDecimal) extraData.get("purchase_value"))),
                        "<current-value>", settings.format(((BigDecimal) extraData.get("current_value"))),
                        "<net-value>", settings.format(((BigDecimal) extraData.get("net_value"))),
                        "<server-currency>", plugin.getEconomy().currencyNamePlural()
                )
        );
    }

    private Map<String, StockData> getData(UUID uuid) {
        StockPlayer stockPlayer = playerManager.forceGetStockPlayer(uuid);
        if (stockPlayer == null) {
            return Collections.emptyMap();
        }

        return stockPlayer.getStockMap();
    }

    private static class StockComparator implements Comparator<Quote> {

        @Override
        public int compare(Quote o1, Quote o2) {
            if (o1 == o2) {
                return 0;
            }

            if (o1 == null) {
                return -1;
            }

            if (o2 == null) {
                return 1;
            }

            return o1.getSymbol().toUpperCase().compareTo(o2.getSymbol().toUpperCase());
        }
    }
}
