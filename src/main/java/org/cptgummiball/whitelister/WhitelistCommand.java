package org.cptgummiball.whitelister;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WhitelistCommand implements CommandExecutor, TabCompleter {
    private WhitelistManager whitelistManager;
    private final Whitelister plugin;

    public WhitelistCommand(Whitelister plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
                listPendingApplications(player);
            } else if (args[0].equalsIgnoreCase("accept") && args.length == 2) {
                acceptApplication(player, args[1]);
            }
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
        String noApp = plugin.getConfig().getString("messages.no_applications", null);
        String appList = plugin.getConfig().getString("messages.application_list", null);
        Map<String, UUID> applications = whitelistManager.getPendingApplications();
        if (applications.isEmpty()) {
            player.sendMessage(noApp);
        } else {
            player.sendMessage(appList);
            for (String username : applications.keySet()) {
                player.sendMessage("- " + username);
            }
        }
    }

    private void acceptApplication(Player player, String username) {
        String appAccept = plugin.getConfig().getString("messages.application_accepted", null);
        String noAppFound = plugin.getConfig().getString("messages.no_application_found", null);
        Map<String, UUID> applications = whitelistManager.getPendingApplications();
        if (applications.containsKey(username)) {
            whitelistManager.acceptApplication(username);
            player.sendMessage(appAccept.replace("{username}", username));
        } else {
            player.sendMessage(noAppFound.replace("{username}", username));
        }
    }
}