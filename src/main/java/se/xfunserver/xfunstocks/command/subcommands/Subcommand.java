package se.xfunserver.xfunstocks.command.subcommands;

import org.bukkit.entity.Player;

import java.util.List;

public interface Subcommand {

    void onCommand(Player player, String[] args);

    int minArgs();

    int maxArgs();

    String commandName();

    String requiredPerm();

    List<String> commandHelpKeys(Player player);

    boolean canPlayerExecute(Player player);

    boolean shouldTabCompleterReturnPlayerList(Player player);

    void sendPending(Player player);
}
