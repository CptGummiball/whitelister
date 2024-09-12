package org.cptgummiball.whitelister;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final Whitelister plugin;

    public PlayerJoinListener(Whitelister plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        FileConfiguration config = plugin.getConfig();
        String pendingApp = plugin.getConfig().getString("messages.pending_applications_notification", null);
        // Check if notifications are enabled and player has permission
        if (config.getBoolean("notifications.enabled", true) && player.hasPermission("whitelister.manage")) {
            if (plugin.getWhitelistManager().getPendingApplications() != null) {
                player.sendMessage(ChatColor.GOLD + pendingApp);
            }
        }
    }
}
