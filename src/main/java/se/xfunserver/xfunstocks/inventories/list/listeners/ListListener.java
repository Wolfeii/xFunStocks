package se.xfunserver.xfunstocks.inventories.list.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import se.xfunserver.xfunstocks.inventories.list.ListInventory;
import se.xfunserver.xfunstocks.processor.types.PurchaseProcessor;
import se.xfunserver.xfunstocks.processor.types.SaleProcessor;

public record ListListener(ListInventory inventory,
                           PurchaseProcessor purchaseProcessor, SaleProcessor saleProcessor)
        implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)
                || inventory.isNotViewing(e.getWhoClicked())) {
            return;
        }

        String symbol = inventory.getSymbol(e.getRawSlot());
        if (symbol == null) {
            return;
        }

        processClick(e.getClick(), player, symbol);
    }

    private void processClick(ClickType click, Player player, String symbol) {
        switch (click) {
            case SHIFT_LEFT -> purchaseProcessor.processTransaction(player, symbol, 5);
            case LEFT -> purchaseProcessor.processTransaction(player, symbol, 1);
            case RIGHT -> saleProcessor.processTransaction(player, symbol, 1);
            case SHIFT_RIGHT -> saleProcessor.processTransaction(player, symbol, 5);
        }
    }
}