package se.xfunserver.xfunstocks.player.listeners;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import se.xfunserver.xfunstocks.player.PlayerManager;

public record PlayerListener(PlayerManager playerManager) implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getLogger().info(event.getPlayer().getName() + " cachades.");
        event.getPlayer().setDisplayName(StringUtils.reverse(event.getPlayer().getName()));
        playerManager.cachePlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Bukkit.getLogger().info(event.getPlayer().getName() + " togs bort ur cachen.");
        playerManager.uncachePlayer(event.getPlayer().getUniqueId());
    }
}
