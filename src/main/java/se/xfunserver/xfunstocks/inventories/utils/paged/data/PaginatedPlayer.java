package se.xfunserver.xfunstocks.inventories.utils.paged.data;

import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class PaginatedPlayer {

    private final Map<Integer, Inventory> pageMap = new HashMap<>();
    private int currentPage = 1;

    public void addInventory(int page, Inventory inventory) {
        pageMap.put(page, inventory);
    }

    public Inventory getPreviousPage() {
        currentPage--;
        return pageMap.get(currentPage);
    }

    public Inventory getNextPage() {
        currentPage++;
        return pageMap.get(currentPage);
    }
}