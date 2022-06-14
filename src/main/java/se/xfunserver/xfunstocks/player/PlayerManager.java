package se.xfunserver.xfunstocks.player;

import org.bukkit.Bukkit;
import se.xfunserver.xfunstocks.config.Settings;
import se.xfunserver.xfunstocks.player.listeners.PlayerListener;
import se.xfunserver.xfunstocks.player.player.StockPlayer;
import se.xfunserver.xfunstocks.player.player.data.StockData;
import se.xfunserver.xfunstocks.stocks.StockManager;
import se.xfunserver.xfunstocks.storage.Storage;
import se.xfunserver.xfunstocks.transactions.Transaction;
import se.xfunserver.xfunstocks.xFunStocks;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {

    private final Map<UUID, Instant> lastActionMap = new ConcurrentHashMap<>();
    private final Map<UUID, StockPlayer> stockPlayerMap = new ConcurrentHashMap<>();

    private final xFunStocks plugin;
    private final StockManager stockManager;
    private final Storage storage;
    private final Settings settings;

    public PlayerManager(xFunStocks plugin, StockManager stockManager, Storage storage,
                         Settings settings) {
        this.plugin = plugin;
        this.stockManager = stockManager;
        this.storage = storage;
        this.settings = settings;

        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), plugin);
    }

    public void cachePlayer(UUID uuid) {
        getOrCreateStockPlayer(uuid);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> loadPlayerTransactions(uuid));
    }

    public StockPlayer getStockPlayer(UUID uuid) {
        return stockPlayerMap.get(uuid);
    }

    public StockPlayer forceGetStockPlayer(UUID uuid) {
        StockPlayer cachedPlayer = stockPlayerMap.get(uuid);
        if (cachedPlayer != null) {
            return cachedPlayer;
        }

        loadPlayerTransactions(uuid);
        return getAndRemoveStockPlayer(uuid);
    }

    public BigDecimal getCurrentValue(StockPlayer stockPlayer) {
        BigDecimal currentValue = BigDecimal.ZERO;
        for (Map.Entry<String, StockData> e : stockPlayer.getStockMap().entrySet()) {
            BigDecimal serverPrice = stockManager.getServerPrice(e.getKey());
            BigDecimal quantity = BigDecimal.valueOf(e.getValue().getQuantity());
            BigDecimal totalValue = serverPrice.multiply(quantity);

            currentValue = currentValue.add(totalValue);
        }

        return currentValue;
    }

    public void uncachePlayer(UUID uuid) {
        stockPlayerMap.remove(uuid);
        lastActionMap.remove(uuid);
    }

    public boolean canNotPerformTransaction(UUID uuid) {
        if (settings.getTransactionCooldownSeconds() == 0) {
            return false;
        }

        Long elapsedSeconds = getSecondsSinceLastAction(uuid);
        if (elapsedSeconds == null) {
            return false;
        }

        return elapsedSeconds < settings.getTransactionCooldownSeconds();
    }

    private Long getSecondsSinceLastAction(UUID uuid) {
        Instant lastAction = lastActionMap.get(uuid);
        if (lastAction == null) {
            return null;
        }

        return Duration.between(lastAction, Instant.now()).toSeconds();
    }

    private StockPlayer getAndRemoveStockPlayer(UUID uuid) {
        StockPlayer player = getStockPlayer(uuid);
        uncachePlayer(uuid);

        return player;
    }

    public void registerTransaction(UUID uuid, Transaction transaction) {
        StockPlayer player = getOrCreateStockPlayer(uuid);
        player.addTransaction(transaction);

        lastActionMap.put(uuid, Instant.now());
    }

    private void loadPlayerTransactions(UUID uuid) {
        storage
                .getPlayerTransactions(uuid)
                .forEach(transaction -> registerTransaction(uuid, transaction));
    }

    private StockPlayer getOrCreateStockPlayer(UUID uuid) {
        return stockPlayerMap.computeIfAbsent(uuid, k -> new StockPlayer());
    }

}
