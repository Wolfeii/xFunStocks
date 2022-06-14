package se.xfunserver.xfunstocks.command.subcommands.types.menus;

import org.bukkit.entity.Player;
import se.xfunserver.xfunstocks.command.subcommands.common.TargetableCommand;
import se.xfunserver.xfunstocks.config.Messages;
import se.xfunserver.xfunstocks.inventories.InventoryManager;
import se.xfunserver.xfunstocks.xFunStocks;

import java.util.UUID;

public class PortfolioCommand extends TargetableCommand {

    public PortfolioCommand(xFunStocks plugin, InventoryManager inventoryManager, Messages messages) {
        super(plugin, inventoryManager, messages);
    }

    @Override
    public String commandName() {
        return "portfolio";
    }

    @Override
    public void callerAction(Player caller) {
        inventoryManager.openPortfolioInventory(caller);
    }

    @Override
    public void targetAction(Player caller, UUID target) {
        inventoryManager.openPortfolioInventory(caller ,target);
    }
}
