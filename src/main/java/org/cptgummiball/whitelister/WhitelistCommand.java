package org.cptgummiball.whitelister;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WhitelistCommand implements CommandExecutor, TabCompleter {
    private WhitelistManager whitelistManager;
    private final Whitelister plugin;
    private final FileConfiguration messages;

    public WhitelistCommand(Whitelister plugin) {
        this.plugin = plugin;
        this.whitelistManager = whitelistManager;
        this.messages = plugin.getLanguage().equalsIgnoreCase("de")
                ? plugin.getConfig("messages_de.yml") : plugin.getConfig("messages_en.yml");

    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {

            listPendingApplications(player);
        } else if (args[0].equalsIgnoreCase("accept") && args.length == 2) {
            acceptApplication(player, args[1]);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }

        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("list");
            completions.add("accept");
        } else if (args.length == 2 && "accept".equalsIgnoreCase(args[0])) {
            // Provide tab completion for usernames
            completions.addAll(whitelistManager.getPendingApplications().keySet());
        }

        return completions;
    }

    private void listPendingApplications(Player player) {
        Map<String, UUID> applications = whitelistManager.getPendingApplications();
        if (applications.isEmpty()) {
            player.sendMessage(ChatColor.RED + messages.getString("no_applications"));
        } else {
            player.sendMessage(ChatColor.GREEN + messages.getString("application_list"));
            for (String username : applications.keySet()) {
                player.sendMessage("- " + username);
            }
        }
    }

    private void acceptApplication(Player player, String username) {
        Map<String, UUID> applications = whitelistManager.getPendingApplications();
        if (applications.containsKey(username)) {
            whitelistManager.acceptApplication(username);
            player.sendMessage(ChatColor.GREEN + messages.getString("application_accepted").replace("{username}", username));
        } else {
            player.sendMessage(ChatColor.RED + messages.getString("no_application_found").replace("{username}", username));
        }
    }
}