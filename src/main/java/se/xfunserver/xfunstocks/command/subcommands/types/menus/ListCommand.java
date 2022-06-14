package se.xfunserver.xfunstocks.command.subcommands.types.menus;

import org.bukkit.entity.Player;
import se.xfunserver.xfunstocks.command.subcommands.common.BaseCommand;
import se.xfunserver.xfunstocks.config.Messages;
import se.xfunserver.xfunstocks.inventories.InventoryManager;

public class ListCommand extends BaseCommand {

    private final InventoryManager inventoryManager;

    public ListCommand(InventoryManager inventoryManager, Messages messages) {
        super(messages);

        this.inventoryManager = inventoryManager;
    }

    @Override
    public void onCommand(Player player, String[] args) {
        inventoryManager.openListInventory(player);
    }

    @Override
    public String commandName() {
        return "list";
    }
}