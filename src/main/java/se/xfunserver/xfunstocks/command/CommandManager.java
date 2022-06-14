package se.xfunserver.xfunstocks.command;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import se.xfunserver.xfunstocks.command.impl.StockMarketCommand;
import se.xfunserver.xfunstocks.command.impl.StockMarketTabCompleter;
import se.xfunserver.xfunstocks.command.subcommands.Subcommand;
import se.xfunserver.xfunstocks.command.subcommands.types.menus.ListCommand;
import se.xfunserver.xfunstocks.command.subcommands.types.menus.PortfolioCommand;
import se.xfunserver.xfunstocks.command.subcommands.types.transactions.BuyCommand;
import se.xfunserver.xfunstocks.command.subcommands.types.transactions.SellCommand;
import se.xfunserver.xfunstocks.config.Messages;
import se.xfunserver.xfunstocks.inventories.InventoryManager;
import se.xfunserver.xfunstocks.processor.types.PurchaseProcessor;
import se.xfunserver.xfunstocks.processor.types.SaleProcessor;
import se.xfunserver.xfunstocks.utils.Logger;
import se.xfunserver.xfunstocks.xFunStocks;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class CommandManager {

    private static final String DEFAULT_PERM = "stockmarket.use";
    public static final String COMMAND_BYPASS_PERM = "stockmarket.commandbypass";

    private static final String ROOT_COMMAND = "stockmarket";

    private final Map<String, Subcommand> subcommandMap = new LinkedHashMap<>();

    public CommandManager(xFunStocks plugin,
                          InventoryManager inventoryManager,
                          PurchaseProcessor purchaseProcessor,
                          SaleProcessor saleProcessor,
                          Messages messages) {
        PluginCommand pluginCommand = Bukkit.getPluginCommand(ROOT_COMMAND);
        if (pluginCommand == null) {
            Logger.severe("failed to find command");
            return;
        }

        registerSubcommands(
                new PortfolioCommand(plugin, inventoryManager, messages),
                new BuyCommand(purchaseProcessor, messages),
                new SellCommand(saleProcessor, messages),
                new ListCommand(inventoryManager, messages)
        );

        pluginCommand.setExecutor(new StockMarketCommand(this, messages));
        pluginCommand.setTabCompleter(new StockMarketTabCompleter(this));
    }

    public static boolean doesNotHaveBasePermission(Player player) {
        return !player.hasPermission(DEFAULT_PERM);
    }

    public Collection<Subcommand> getRegisteredSubcommands() {
        return subcommandMap.values();
    }

    public Subcommand findSubcommand(String subcommand) {
        String lowercaseSubcommand = subcommand.toLowerCase();
        return subcommandMap.get(lowercaseSubcommand);
    }

    private void registerSubcommands(Subcommand... subcommands) {
        for (Subcommand subcommand : subcommands) {
            subcommandMap.put(subcommand.commandName(), subcommand);
        }
    }
}