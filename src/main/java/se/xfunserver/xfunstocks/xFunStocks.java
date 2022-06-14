package se.xfunserver.xfunstocks;

import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import pl.zankowski.iextrading4j.client.IEXCloudClient;
import pl.zankowski.iextrading4j.client.IEXCloudTokenBuilder;
import pl.zankowski.iextrading4j.client.IEXTradingApiVersion;
import pl.zankowski.iextrading4j.client.IEXTradingClient;
import se.xfunserver.xfunstocks.command.CommandManager;
import se.xfunserver.xfunstocks.config.Messages;
import se.xfunserver.xfunstocks.config.Settings;
import se.xfunserver.xfunstocks.inventories.InventoryManager;
import se.xfunserver.xfunstocks.player.PlayerManager;
import se.xfunserver.xfunstocks.processor.types.PurchaseProcessor;
import se.xfunserver.xfunstocks.processor.types.SaleProcessor;
import se.xfunserver.xfunstocks.signs.SignManager;
import se.xfunserver.xfunstocks.stocks.StockManager;
import se.xfunserver.xfunstocks.storage.Storage;

@Getter
public final class xFunStocks extends JavaPlugin {

    private Economy economy;
    private CommandManager commandManager;
    private StockManager stockManager;
    private SignManager signManager;

    private PurchaseProcessor purchaseProcessor;
    private SaleProcessor saleProcessor;

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            this.getLogger().severe("Hittade inte pluginet Vault, vilket krävs för att xFunStocks skall köras.");
            this.setEnabled(false);
            return;
        }

        Settings settings = new Settings(this);
        Storage storage = Storage.buildStorage(settings);
        Messages messages = new Messages(this, settings);

        stockManager = new StockManager(settings);
        PlayerManager playerManager = new PlayerManager(this, stockManager, storage, settings);

        purchaseProcessor =
                new PurchaseProcessor(this, stockManager,
                        playerManager, storage, settings, messages);
        saleProcessor =
                new SaleProcessor(this, stockManager,
                        playerManager, storage, settings, messages);

        InventoryManager inventoryManager =
                new InventoryManager(
                        this,
                        playerManager,
                        stockManager,
                        purchaseProcessor,
                        saleProcessor,
                        messages,
                        storage,
                        settings);
        signManager = new SignManager(this, settings, stockManager, inventoryManager);

        this.commandManager =
                new CommandManager(this, inventoryManager, purchaseProcessor,
                        saleProcessor, messages);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        signManager.saveAll();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp =
                getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();
        return true;
    }

    public StockManager getStockManager() {
        return stockManager;
    }
}
