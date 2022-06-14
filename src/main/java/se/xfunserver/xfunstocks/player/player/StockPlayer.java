package se.xfunserver.xfunstocks.player.player;

import lombok.Getter;
import se.xfunserver.xfunstocks.player.player.data.StockData;
import se.xfunserver.xfunstocks.transactions.Transaction;
import se.xfunserver.xfunstocks.transactions.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.NavigableMap;
import java.util.TreeMap;

@Getter
public class StockPlayer {

    private final NavigableMap<String, StockData> stockMap = new TreeMap<>();
    private final NavigableMap<Instant, Transaction> transactionMap = new TreeMap<>();

    private BigDecimal portfolioValue = BigDecimal.ZERO;

    public void addTransaction(Transaction transaction) {
        String symbol = transaction.getSymbol().toUpperCase();
        StockData stockData = stockMap.getOrDefault(symbol, new StockData());
        if (transaction.getType() == TransactionType.PURCHASE) {
            portfolioValue = portfolioValue.add(transaction.getStockValue());
            stockData.increase(transaction);
        } else if (transaction.getType() == TransactionType.SALE) {
            portfolioValue = portfolioValue.subtract(transaction.getStockValue());
            stockData.decrease(transaction);
        }

        transactionMap.put(transaction.getDate(), transaction);
        stockMap.put(symbol, stockData);
    }

    public BigDecimal getProfitMargin(BigDecimal currentValue) {
        return currentValue.subtract(portfolioValue);
    }

    public Collection<Transaction> getTransactions() {
        return transactionMap.values();
    }
}
