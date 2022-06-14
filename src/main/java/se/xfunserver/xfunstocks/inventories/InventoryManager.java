package se.xfunserver.xfunstocks.inventories;

import org.bukkit.entity.Player;
import se.xfunserver.xfunstocks.config.Messages;
import se.xfunserver.xfunstocks.config.Settings;
import se.xfunserver.xfunstocks.config.common.ConfigSection;
import se.xfunserver.xfunstocks.inventories.list.ListInventory;
import se.xfunserver.xfunstocks.inventories.portfolio.PortfolioInventory;
import se.xfunserver.xfunstocks.player.PlayerManager;
import se.xfunserver.xfunstocks.processor.types.PurchaseProcessor;
import se.xfunserver.xfunstocks.processor.types.SaleProcessor;
import se.xfunserver.xfunstocks.stocks.StockManager;
import se.xfunserver.xfunstocks.storage.Storage;
import se.xfunserver.xfunstocks.xFunStocks;

import java.util.UUID;

public class InventoryManager {

    private final PortfolioInventory portfolioInventory;
    private final ListInventory listInventory;

    public InventoryManager(
            xFunStocks stockMarket,
            PlayerManager playerManager,
            StockManager stockManager,
            PurchaseProcessor processor,
            SaleProcessor saleProcessor,
            Messages messages,
            Storage storage,
            Settings settings) {

        this.listInventory =
                new ListInventory(
                        stockMarket,
                        processor,
                        saleProcessor,
                        new ConfigSection(stockMarket, "list"));

        this.portfolioInventory =
                new PortfolioInventory(
                        stockMarket,
                        messages,
                        playerManager,
                        stockManager,
                        settings,
                        new ConfigSection(stockMarket, "portfolio"));
    }
    public void openListInventory(Player player) {
        listInventory.openInventory(player);
    }

    public void openPortfolioInventory(Player player) {
        portfolioInventory.displayInventory(player, player.getUniqueId());
    }

    public void openPortfolioInventory(Player player, UUID target) {
        portfolioInventory.displayInventory(player, target);
    }
}
