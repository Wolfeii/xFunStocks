package se.xfunserver.xfunstocks.processor.model;

import lombok.Data;
import org.bukkit.entity.Player;
import pl.zankowski.iextrading4j.api.stocks.Quote;
import se.xfunserver.xfunstocks.player.player.StockPlayer;
import se.xfunserver.xfunstocks.transactions.Transaction;

import java.math.BigDecimal;
import java.util.Collection;

@Data
public class ProcessorContext {

    private final Player player;
    private final String symbol;
    private final int quantity;

    private Quote stock;
    private BigDecimal serverPrice;
    private StockPlayer stockPlayer;
    private Collection<Transaction> processedTransactions;

    private BigDecimal quantityPrice;
    private BigDecimal brokerFees = BigDecimal.ZERO;
    private BigDecimal grandTotal;
    private BigDecimal soldValue;
    private BigDecimal net;

    private Transaction transaction;

    public void setServerPrice(BigDecimal serverPrice) {
        this.serverPrice = serverPrice;
        this.quantityPrice = serverPrice.multiply(BigDecimal.valueOf(quantity));
    }
}