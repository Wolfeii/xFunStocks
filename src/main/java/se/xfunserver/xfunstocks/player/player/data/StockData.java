package se.xfunserver.xfunstocks.player.player.data;

import lombok.Getter;
import se.xfunserver.xfunstocks.transactions.Transaction;

import java.math.BigDecimal;

@Getter
public class StockData {
    
    private int quantity = 0;
    private BigDecimal value = BigDecimal.ZERO;

    public void increase(Transaction transaction) {
        quantity += transaction.getQuantity();
        value = value.add(transaction.getStockValue());
    }

    public void decrease(Transaction transaction) {
        quantity -= transaction.getQuantity();
        value = value.subtract(transaction.getStockValue());
    }
}
