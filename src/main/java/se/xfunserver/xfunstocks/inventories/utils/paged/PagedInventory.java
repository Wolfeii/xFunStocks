package se.xfunserver.xfunstocks.inventories.utils.paged;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import se.xfunserver.xfunstocks.config.Messages;
import se.xfunserver.xfunstocks.config.Settings;
import se.xfunserver.xfunstocks.config.common.ConfigSection;
import se.xfunserver.xfunstocks.inventories.utils.paged.data.PaginatedPlayer;
import se.xfunserver.xfunstocks.inventories.utils.paged.listeners.PagedInventoryListener;
import se.xfunserver.xfunstocks.xFunStocks;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

public abstract class PagedInventory<L, K, V> {

    protected final xFunStocks plugin;
    protected final Settings settings;
    private final Messages messages;

    private final Map<UUID, PaginatedPlayer> playerMap = new HashMap<>();

    private final String name;
    private final int size;
    private final ItemStack baseItem;

    private final int previousPageSlot;
    private final ItemStack previousPageStack;
    private final ItemStack noPreviousPageStack;

    private final int nextPageSlot;
    private final ItemStack nextPageStack;
    private final ItemStack noNextPageStack;

    private final List<Integer> contentSlots;
    private final int contentPerPage;

    private final Map<Integer, ItemStack> extraItems = new HashMap<>();

    protected PagedInventory(xFunStocks plugin, Messages messages, Settings settings,
                             ConfigSection section) {
        this.plugin = plugin;
        this.messages = messages;
        this.settings = settings;

        this.name = section.getString("name");
        this.size = section.getInt("size");
        this.baseItem = section.getItemStack("base-item");

        this.previousPageSlot = section.getInt("previous-page.slot");
        this.previousPageStack = section.getItemStack("previous-page.previous-page");
        this.noPreviousPageStack = section.getItemStack("previous-page.no-previous");

        this.nextPageSlot = section.getInt("next-page.slot");
        this.nextPageStack = section.getItemStack("next-page.next-page");
        this.noNextPageStack = section.getItemStack("next-page.no-next");

        this.contentSlots = section.getIntegerList("content-slots");
        this.contentPerPage = contentSlots.size();

        ConfigSection extraSection = section.getSection("extra-items");
        if (extraSection != null) {
            for (String key : extraSection.getKeys()) {
                extraItems.put(Integer.valueOf(key), extraSection.getItemStack(key));
            }
        }

        Bukkit.getPluginManager().registerEvents(new PagedInventoryListener(this), plugin);
    }

    public void displayInventory(Player player, L target) {
        Bukkit.getScheduler()
                .runTaskAsynchronously(
                        plugin,
                        () -> {
                            Map<K, V> data = getContent(target);
                            if (data.isEmpty()) {
                                messages.sendNoContent(player);
                                return;
                            }

                            Map<String, Object> extraData = getExtraData(target);
                            Bukkit.getScheduler()
                                    .runTask(
                                            plugin,
                                            () -> {
                                                PaginatedPlayer paginatedPlayer = new PaginatedPlayer();

                                                int currentPage = 1;
                                                int currentIndex = 0;
                                                int totalDisplayed = 0;
                                                Inventory inventory = Bukkit.createInventory(null, size, name);
                                                for (Map.Entry<K, V> e : data.entrySet()) {
                                                    inventory.setItem(
                                                            contentSlots.get(currentIndex),
                                                            getContentStack(baseItem, e.getKey(), e.getValue())
                                                    );
                                                    totalDisplayed++;

                                                    if ((currentIndex + 1) == contentPerPage
                                                            || totalDisplayed == data.size()) {
                                                        inventory.setItem(
                                                                previousPageSlot,
                                                                currentPage == 1 ? noPreviousPageStack : previousPageStack
                                                        );

                                                        inventory.setItem(
                                                                nextPageSlot,
                                                                totalDisplayed < data.size()
                                                                        ? nextPageStack
                                                                        : noNextPageStack
                                                        );

                                                        for (Map.Entry<Integer, ItemStack> extraEntry : extraItems.entrySet()) {
                                                            inventory.setItem(
                                                                    extraEntry.getKey(),
                                                                    getExtraItem(extraEntry.getValue(), extraData)
                                                            );
                                                        }

                                                        paginatedPlayer.addInventory(currentPage, inventory);
                                                        if (currentPage == 1) {
                                                            player.openInventory(inventory);
                                                            playerMap.put(player.getUniqueId(), paginatedPlayer);
                                                        }

                                                        inventory = Bukkit.createInventory(null, size, name);

                                                        currentPage++;
                                                        currentIndex = 0;
                                                        continue;
                                                    }

                                                    currentIndex++;
                                                }
                                            }
                                    );
                        }
                );
    }

    public void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player clicker) || event.getClickedInventory() == null) {
            return;
        }

        PaginatedPlayer player = playerMap.get(clicker.getUniqueId());
        if (player == null) {
            return;
        }

        event.setCancelled(true);
        if (event.getRawSlot() >= size) {
            return;
        }

        ItemStack selectedStack = event.getClickedInventory().getItem(event.getSlot());
        if (selectedStack == null
                || selectedStack.isSimilar(noNextPageStack)
                || selectedStack.isSimilar(noPreviousPageStack)) {
            return;
        }

        if (event.getSlot() == previousPageSlot) {
            clicker.openInventory(player.getPreviousPage());
            playerMap.put(clicker.getUniqueId(), player);
        } else if (event.getSlot() == nextPageSlot) {
            clicker.openInventory(player.getNextPage());
            playerMap.put(clicker.getUniqueId(), player);
        }
    }

    protected abstract Map<K, V> getContent(L lookup);

    protected abstract ItemStack getContentStack(ItemStack baseStack, K key, V value);

    protected Map<String, Object> getExtraData(L lookup) {
        return Collections.emptyMap();
    }

    protected ItemStack getExtraItem(ItemStack baseStack, Map<String, Object> extraData) {
        return baseStack;
    }

    public boolean hasInventory(HumanEntity entity) {
        return playerMap.containsKey(entity.getUniqueId());
    }

    public void remove(HumanEntity entity) {
        playerMap.remove(entity.getUniqueId());
    }

    protected final String formatInstant(Instant instant) {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .withLocale(settings.getLocale())
                .withZone(ZoneId.systemDefault())
                .format(instant);
    }
}
