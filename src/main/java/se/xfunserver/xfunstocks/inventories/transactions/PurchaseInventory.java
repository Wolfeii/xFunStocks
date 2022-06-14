package se.xfunserver.xfunstocks.inventories.transactions;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pl.zankowski.iextrading4j.api.stocks.Quote;
import se.xfunserver.xfunstocks.config.Messages;
import se.xfunserver.xfunstocks.config.Settings;
import se.xfunserver.xfunstocks.config.common.ConfigSection;
import se.xfunserver.xfunstocks.processor.StockProcessor;
import se.xfunserver.xfunstocks.processor.types.PurchaseProcessor;
import se.xfunserver.xfunstocks.stocks.StockManager;
import se.xfunserver.xfunstocks.transactions.TransactionType;
import se.xfunserver.xfunstocks.utils.Utils;
import se.xfunserver.xfunstocks.xFunStocks;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class PurchaseInventory {

    private final xFunStocks stockMarket;
    
    private final TransactionType transactionType;
    private final ConfigSection configSection;
    private final StockProcessor processor;
    private final Settings settings;

    private final Player player;

    private String symbol;
    private int quantity;

    private Gui inventory;

    public PurchaseInventory(xFunStocks stockMarket, Settings settings, Player player,
                             TransactionType transactionType, StockProcessor purchaseProcessor,
                             String symbol) {
        this.player = player;
        this.stockMarket = stockMarket;
        this.transactionType = transactionType;
        this.settings = settings;
        this.processor = purchaseProcessor;
        this.configSection = new ConfigSection(stockMarket, "transactions");
        this.symbol = symbol;
        this.quantity = 1;

        this.createInventory();
    }

    private void createInventory() {
        String inventoryName = "";
        if (this.transactionType.equals(TransactionType.PURCHASE)) {
            inventoryName = Utils.color(configSection.getString("buy-name"))
                    .replace("<symbol>", symbol);
        } else if (this.transactionType.equals(TransactionType.SALE)) {
            inventoryName = Utils.color(configSection.getString("sell-name"))
                    .replace("<symbol>", symbol);
        }
        
        Quote quote = stockMarket.getStockManager().getStock(symbol);

        inventory = Gui.gui()
                .title(Component.text(inventoryName))
                .rows(4)
                .create();

        inventory.setItem(22, ItemBuilder.from(Utils.createItemStack(configSection.getSection("items.confirm"),
                buildStockDataMap(quote, stockMarket.getStockManager().getServerPrice(quote), quantity))).asGuiItem(click -> {

            player.closeInventory();
            processor.processTransaction(player, symbol, quantity);
        }));

        inventory.setItem(4, ItemBuilder.from(Utils.createItemStack(configSection.getSection("items.item"),
                buildStockDataMap(quote, stockMarket.getStockManager().getServerPrice(quote), quantity))).asGuiItem());


        this.handleQuantityChange(quote);
    }

    protected Map<String, Object> buildStockDataMap(Quote quote, BigDecimal serverPrice, int quantity) {
        return Map.ofEntries(
                Map.entry("<quantity>", quantity),
                Map.entry("<total>", serverPrice.multiply(BigDecimal.valueOf(quantity))),
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

    public void handleQuantityChange(Quote quote) {
        if (getQuantity() > 1) { // If we have more than one item, we should be able to remove one.
            this.getInventory().setItem(0, ItemBuilder.from(configSection.getItemStack("items.remove1")).asGuiItem(click -> {
                this.quantity -= 1;
                handleQuantityChange(quote);
                click.setCancelled(true);
            }));
        } else {
            this.getInventory().setItem(0, new GuiItem(Material.AIR));
        }

        if (getQuantity() > 10) { // If we have more than one item, we should be able to remove one.
            this.getInventory().setItem(9, ItemBuilder.from(configSection.getItemStack("items.remove10")).asGuiItem(click -> {
                this.quantity -= 10;
                handleQuantityChange(quote);
                click.setCancelled(true);
            }));
        } else {
            this.getInventory().setItem(9, new GuiItem(Material.AIR));
        }

        if (getQuantity() > 100) { // If we have more than one item, we should be able to remove one.
            this.getInventory().setItem(18, ItemBuilder.from(configSection.getItemStack("items.remove100")).asGuiItem(click -> {
                this.quantity -= 100;
                handleQuantityChange(quote);
                click.setCancelled(true);
            }));
        } else {
            this.getInventory().setItem(18, new GuiItem(Material.AIR));
        }

        this.getInventory().setItem(26, ItemBuilder.from(configSection.getItemStack("items.add100")).asGuiItem(click -> {
            this.quantity += 100;
            handleQuantityChange(quote);
            click.setCancelled(true);
        }));

        this.getInventory().setItem(17, ItemBuilder.from(configSection.getItemStack("items.add10")).asGuiItem(click -> {
            this.quantity += 10;
            handleQuantityChange(quote);
            click.setCancelled(true);
        }));

        this.getInventory().setItem(8, ItemBuilder.from(configSection.getItemStack("items.add1")).asGuiItem(click -> {
            this.quantity += 1;
            handleQuantityChange(quote);
            click.setCancelled(true);
        }));

        inventory.setItem(4, ItemBuilder.from(Utils.createItemStack(configSection.getSection("items.item"),
                buildStockDataMap(quote, stockMarket.getStockManager().getServerPrice(quote), quantity))).asGuiItem());


        inventory.setItem(22, ItemBuilder.from(Utils.createItemStack(configSection.getSection("items.confirm"),
                buildStockDataMap(quote, stockMarket.getStockManager().getServerPrice(quote), quantity))).asGuiItem(click -> {

            player.closeInventory();
            processor.processTransaction(player, symbol, quantity);
        }));

        getInventory().update();
    }

    private boolean shouldSlotBeEmpty(int slot) {
        return slot >= 0 &&
                (this.getInventory().getInventory().getItem(slot) == null ||
                this.getInventory().getInventory().getItem(slot).getType() == Material.AIR);
    }

    public Gui getInventory() {
        return inventory;
    }

    public int getQuantity() {
        return quantity;
    }
}
