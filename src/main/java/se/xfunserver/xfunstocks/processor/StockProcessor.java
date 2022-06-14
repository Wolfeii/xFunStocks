package se.xfunserver.xfunstocks.processor;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import pl.zankowski.iextrading4j.api.stocks.Quote;
import se.xfunserver.xfunstocks.config.Messages;
import se.xfunserver.xfunstocks.config.Settings;
import se.xfunserver.xfunstocks.player.PlayerManager;
import se.xfunserver.xfunstocks.player.player.StockPlayer;
import se.xfunserver.xfunstocks.processor.model.ProcessorContext;
import se.xfunserver.xfunstocks.stocks.StockManager;
import se.xfunserver.xfunstocks.storage.Storage;
import se.xfunserver.xfunstocks.transactions.Transaction;
import se.xfunserver.xfunstocks.xFunStocks;

import java.math.BigDecimal;

@RequiredArgsConstructor
public abstract class StockProcessor {

    protected final xFunStocks stockMarket;
    protected final StockManager stockManager;
    protected final PlayerManager playerManager;
    protected final Storage storage;
    protected final Settings settings;
    protected final Messages messages;

    protected abstract boolean shouldBlockStockPlayer(ProcessorContext context);

    protected abstract void calculateTotals(ProcessorContext context);

    protected abstract boolean hasInsufficientFunds(ProcessorContext context);

    protected abstract Transaction buildTransaction(ProcessorContext context);

    protected abstract Event buildEvent(ProcessorContext context);

    protected abstract void processVault(ProcessorContext context);

    protected abstract void sendMessage(ProcessorContext context);

    public void processTransaction(Player player, String symbol, int quantity) {
        ProcessorContext context = new ProcessorContext(player, symbol, quantity);
        StockPlayer stockPlayer = getStockPlayer(context);

        if (stockPlayer == null) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(stockMarket, () -> {
            Quote stock = lookupStock(context);
            if (stock == null) {
                messages.sendInvalidStock(context.getPlayer());
                return;
            }

            calculateTotals(context);

            Bukkit.getScheduler().runTask(stockMarket, () -> {
                if (hasInsufficientFunds(context)) {
                    messages.sendInsufficientFunds(player);
                    return;
                }

                Transaction transaction = buildTransaction(context);
                context.setTransaction(transaction);

                processVault(context);
                sendMessage(context);
                Bukkit.getPluginManager().callEvent(buildEvent(context));
                playerManager.registerTransaction(context.getPlayer().getUniqueId(), transaction);

                Bukkit.getScheduler().runTaskAsynchronously(stockMarket,
                        () -> storage.processTransaction(transaction));
            });
        });
    }

    private StockPlayer getStockPlayer(ProcessorContext context) {
        if (playerManager.canNotPerformTransaction(context.getPlayer().getUniqueId())) {
            messages.sendCooldownMessage(context.getPlayer());
            return null;
        }

        StockPlayer stockPlayer = lookupStockPlayer(context);
        if (stockPlayer == null) {
            return null;
        }

        boolean shouldBlockTransaction = shouldBlockStockPlayer(context);
        if (shouldBlockTransaction) {
            messages.sendInvalidSale(context.getPlayer());
            return null;
        }

        return stockPlayer;
    }

    private StockPlayer lookupStockPlayer(ProcessorContext context) {
        StockPlayer stockPlayer = playerManager.getStockPlayer(context.getPlayer().getUniqueId());
        if (stockPlayer == null) {
            return null;
        }

        context.setStockPlayer(stockPlayer);
        return stockPlayer;
    }

    private Quote lookupStock(ProcessorContext context) {
        Quote quote = stockManager.getStock(context.getSymbol());
        if (stockManager.canNotUseStock(context.getPlayer(), quote)) {
            return null;
        }

        BigDecimal price = stockManager.getServerPrice(quote);
        if (price == null) {
            return null;
        }

        context.setStock(quote);
        context.setServerPrice(price);
        return quote;
    }
}
