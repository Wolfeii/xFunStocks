package se.xfunserver.xfunstocks.processor.types;

import org.bukkit.event.Event;
import se.xfunserver.xfunstocks.config.Messages;
import se.xfunserver.xfunstocks.config.Settings;
import se.xfunserver.xfunstocks.events.StockPurchaseEvent;
import se.xfunserver.xfunstocks.player.PlayerManager;
import se.xfunserver.xfunstocks.processor.StockProcessor;
import se.xfunserver.xfunstocks.processor.model.ProcessorContext;
import se.xfunserver.xfunstocks.stocks.StockManager;
import se.xfunserver.xfunstocks.storage.Storage;
import se.xfunserver.xfunstocks.transactions.Transaction;
import se.xfunserver.xfunstocks.transactions.TransactionType;
import se.xfunserver.xfunstocks.xFunStocks;

import java.math.BigDecimal;

public class PurchaseProcessor extends StockProcessor {

    public PurchaseProcessor(xFunStocks stockMarket,
                             StockManager stockManager,
                             PlayerManager playerManager,
                             Storage storage,
                             Settings settings,
                             Messages messages) {
        super(stockMarket, stockManager, playerManager, storage, settings, messages);
    }

    @Override
    protected boolean shouldBlockStockPlayer(ProcessorContext context) {
        return false;
    }

    @Override
    protected void calculateTotals(ProcessorContext context) {
        BigDecimal grandTotal = context.getQuantityPrice();
        context.setGrandTotal(grandTotal);
    }

    @Override
    protected boolean hasInsufficientFunds(ProcessorContext context) {
        return !stockMarket.getEconomy().has(context.getPlayer(), context.getGrandTotal().doubleValue());
    }

    @Override
    protected Transaction buildTransaction(ProcessorContext context) {
        return Transaction.builder()
                .uuid(context.getPlayer().getUniqueId())
                .type(TransactionType.PURCHASE)
                .symbol(context.getStock().getSymbol())
                .quantity(context.getQuantity())
                .singlePrice(context.getServerPrice())
                .grandTotal(context.getGrandTotal())
                .build();
    }

    @Override
    protected Event buildEvent(ProcessorContext context) {
        return new StockPurchaseEvent(
                context.getPlayer(),
                context.getSymbol(),
                context.getQuantity(),
                context.getServerPrice(),
                context.getGrandTotal()
        );
    }

    @Override
    protected void processVault(ProcessorContext context) {
        stockMarket.getEconomy().withdrawPlayer(context.getPlayer(),
                context.getGrandTotal().doubleValue());
    }

    @Override
    protected void sendMessage(ProcessorContext context) {
        messages.sendBoughtStockMessage(context.getPlayer(), context.getStock().getCompanyName(),
                context.getTransaction());
    }
}
