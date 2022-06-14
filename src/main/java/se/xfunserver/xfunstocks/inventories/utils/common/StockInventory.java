package se.xfunserver.xfunstocks.inventories.utils.common;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import se.xfunserver.xfunstocks.inventories.utils.common.listener.StockInventoryListener;
import se.xfunserver.xfunstocks.xFunStocks;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class StockInventory {

    private final Set<UUID> activeViewers = new HashSet<>();

    protected StockInventory(xFunStocks plugin) {
        plugin.getServer().getPluginManager().registerEvents(new StockInventoryListener(this), plugin);
    }

    public void addViewer(HumanEntity entity) {
        activeViewers.add(entity.getUniqueId());
    }

    public boolean isNotViewing(HumanEntity entity) {
        return !activeViewers.contains(entity.getUniqueId());
    }

    public void remove(HumanEntity humanEntity) {
        activeViewers.remove(humanEntity.getUniqueId());
    }
}
