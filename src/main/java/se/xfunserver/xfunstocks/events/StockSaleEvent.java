package se.xfunserver.xfunstocks.events;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

@Getter
public class StockSaleEvent extends StockPurchaseEvent {

    private final BigDecimal initialPurchase;
    private final BigDecimal netOnTransaction;

    @SuppressWarnings("java:S107")
    public StockSaleEvent(
            Player player,
            String stockSymbol,
            int quantity,
            BigDecimal stockValue,
            BigDecimal grandTotal,
            BigDecimal initialPurchase,
            BigDecimal netOnTransaction) {
        super(player, stockSymbol, quantity, stockValue, grandTotal);

        this.initialPurchase = initialPurchase;
        this.netOnTransaction = netOnTransaction;
    }
}