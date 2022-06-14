package se.xfunserver.xfunstocks.inventories.list;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import se.xfunserver.xfunstocks.config.common.ConfigSection;
import se.xfunserver.xfunstocks.inventories.list.listeners.ListListener;
import se.xfunserver.xfunstocks.inventories.utils.common.StockInventory;
import se.xfunserver.xfunstocks.processor.types.PurchaseProcessor;
import se.xfunserver.xfunstocks.processor.types.SaleProcessor;
import se.xfunserver.xfunstocks.xFunStocks;

import java.util.HashMap;
import java.util.Map;

public class ListInventory extends StockInventory {

    private final Inventory inventory;
    private final Map<Integer, String> symbolMap = new HashMap<>();

    public ListInventory(
            xFunStocks plugin,
            PurchaseProcessor purchaseProcessor,
            SaleProcessor saleProcessor,
            ConfigSection section) {
        super(plugin);

        this.inventory = Bukkit.createInventory(null, section.getInt("size"),
                section.getString("name"));

        for (String key : section.getSection("items").getKeys()) {
            int slot = Integer.parseInt(key);
            String symbol = section.getString("items." + key + ".symbol");
            if (symbol != null && !symbol.isEmpty()) {
                symbolMap.put(slot, symbol.toUpperCase());
            }

            inventory.setItem(slot, section.getItemStack("items." + key));
        }

        Bukkit.getServer()
                .getPluginManager()
                .registerEvents(new ListListener(this, purchaseProcessor, saleProcessor),
                        plugin);
    }

    public void openInventory(Player player) {
        player.openInventory(inventory);
        addViewer(player);
    }

    public String getSymbol(int slot) {
        return symbolMap.get(slot);
    }

}