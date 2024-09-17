package org.cptgummiball.whitelister;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;
import java.util.UUID;

public class PlayerJoinListener implements Listener {
    private final Whitelister plugin;

    public PlayerJoinListener(Whitelister plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        FileConfiguration config = plugin.getConfig();
        String pendingApp = config.getString("messages.pending_applications_notification", null);

        // Check if notifications are enabled and player has permission
        if (config.getBoolean("notifications.enabled", true) && player.hasPermission("whitelister.manage")) {
            Map<String, UUID> pendingApplications = plugin.getWhitelistManager().getPendingApplications();

            // Check if pendingApplications is not null and not empty
            if (pendingApplications != null && !pendingApplications.isEmpty()) {
                player.sendMessage(ChatColor.GOLD + pendingApp);
            }
        }
    }
}
