package se.xfunserver.xfunstocks.command.subcommands.common;


import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import se.xfunserver.xfunstocks.config.Messages;
import se.xfunserver.xfunstocks.inventories.InventoryManager;
import se.xfunserver.xfunstocks.xFunStocks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class TargetableCommand extends BaseCommand {

    private static final String OTHER_PERM_SUFFIX = ".other";
    private static final String OTHER_HELP_SUFFIX = "-other";

    private final xFunStocks plugin;
    protected final InventoryManager inventoryManager;

    protected TargetableCommand(xFunStocks plugin, InventoryManager inventoryManager, Messages messages) {
        super(messages);

        this.plugin = plugin;
        this.inventoryManager = inventoryManager;
    }

    public abstract void callerAction(Player caller);

    public abstract void targetAction(Player caller, UUID target);

    @Override
    public void onCommand(Player player, String[] args) {
        if (args.length == 1) {
            sendPending(player);
            callerAction(player);
        } else {
            handleTargetedPlayer(player, args[1]);
        }
    }

    @Override
    public int maxArgs() {
        return 2;
    }

    @Override
    public List<String> commandHelpKeys(Player player) {
        List<String> keys = new ArrayList<>(super.commandHelpKeys(player));
        if (hasOtherPermission(player)) {
            keys.add(commandName() + OTHER_HELP_SUFFIX);
        }

        return keys;
    }

    @Override
    public boolean shouldTabCompleterReturnPlayerList(Player player) {
        return hasOtherPermission(player);
    }

    public void sendPendingOther(Player player) {
        messages.sendPending(player, commandName() + OTHER_HELP_SUFFIX);
    }

    private boolean hasOtherPermission(Player player) {
        String otherPermission = buildOtherPermission();
        return player.hasPermission(otherPermission);
    }

    @SuppressWarnings("deprecation")
    private void handleTargetedPlayer(Player executor, String target) {
        if (!hasOtherPermission(executor)) {
            messages.sendNoPermission(executor);
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(target);
        if (targetPlayer != null) {
            runForTarget(executor, targetPlayer.getUniqueId());
            return;
        }

        Bukkit.getScheduler()
                .runTaskAsynchronously(
                        plugin,
                        () -> {
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(target);

                            runForTarget(executor, offlinePlayer.getUniqueId());
                        });
    }

    private void runForTarget(Player executor, UUID uuid) {
        sendPendingOther(executor);
        targetAction(executor, uuid);
    }

    private String buildOtherPermission() {
        return requiredPerm() + OTHER_PERM_SUFFIX;
    }
}