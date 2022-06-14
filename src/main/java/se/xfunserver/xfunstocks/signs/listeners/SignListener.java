package se.xfunserver.xfunstocks.signs.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import pl.zankowski.iextrading4j.api.stocks.Quote;
import se.xfunserver.xfunstocks.inventories.InventoryManager;
import se.xfunserver.xfunstocks.inventories.transactions.PurchaseInventory;
import se.xfunserver.xfunstocks.signs.SignManager;
import se.xfunserver.xfunstocks.signs.models.QuoteSign;
import se.xfunserver.xfunstocks.transactions.TransactionType;
import se.xfunserver.xfunstocks.utils.Utils;

import java.util.Arrays;
import java.util.List;

public record SignListener(SignManager signManager, InventoryManager inventoryManager)
        implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || event.getClickedBlock().getType().isAir() ||
                event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_AIR))
            return;

        if (event.getClickedBlock().getState() instanceof Sign sign) {
            processEvent(event.getPlayer(), sign, event.getAction());
        }
    }

    @EventHandler
    public void onSignBreak(BlockBreakEvent event) {
        if (!((event.getBlock().getState() instanceof Sign sign) && signManager.isStockSign(sign))) {
            return;
        }

        if (signManager.isStockSign(sign) && event.getPlayer().isSneaking() && event.getPlayer().hasPermission("skystocks.sign.break")) {
            QuoteSign quoteSign = signManager.getSigns().stream().filter(quote -> {
                return quote.getLocation().equals(event.getBlock().getLocation());
            }).findFirst().orElse(null);

            if (quoteSign != null) {
                signManager.deleteStockSign(quoteSign);
                return;
            }
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onSignChanged(SignChangeEvent event) {
        List<String> lines = Arrays.asList(event.getLines());

        if (lines.get(0) == null || !event.getPlayer().hasPermission("skystocks.sign.place")) return;

        if (lines.get(0).contains("SKY STOCKS")) {
            Quote quote = signManager.getStockManager().getStock(lines.get(1));
            if (quote != null) {
                Sign sign = (Sign) event.getBlock().getState();
                QuoteSign quoteSign = new QuoteSign(signManager.getPlugin(), quote.getSymbol(), sign.getLocation());

                Bukkit.getScheduler().runTaskLater(signManager.getPlugin(), () -> {
                    quoteSign.update();
                    this.signManager.registerStockSign(quoteSign);
                }, 20L);
            }
        }
    }


    public void processEvent(Player clicker, Sign target, Action action) {
        if (clicker == null || !signManager.isStockSign(target))
            return;

        QuoteSign quoteSign = signManager.getSigns().stream().filter(sign -> {
            return sign.getLocation().equals(target.getLocation());
        }).findFirst().orElse(null);

        if (quoteSign == null)
            return;

        PurchaseInventory inventory = new PurchaseInventory(signManager.getPlugin(), signManager.getSettings(), clicker,
                action.equals(Action.RIGHT_CLICK_BLOCK) ? TransactionType.PURCHASE : TransactionType.SALE,
                action.equals(Action.RIGHT_CLICK_BLOCK) ? signManager.getPlugin().getPurchaseProcessor()
                        : signManager.getPlugin().getSaleProcessor(),
                quoteSign.getSymbol());

        inventory.getInventory().open(clicker);
    }
}
