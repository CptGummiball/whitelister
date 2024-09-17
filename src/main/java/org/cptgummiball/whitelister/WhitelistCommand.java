package org.cptgummiball.whitelister;

import org.bukkit.command.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WhitelistCommand implements CommandExecutor, TabCompleter {
    private WhitelistManager whitelistManager;
    private final Whitelister plugin;

    public WhitelistCommand(Whitelister plugin, WhitelistManager whitelistManager) {
        this.plugin = plugin;
        this.whitelistManager = whitelistManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("whitelister.manage")) {
            // Ensure commands are valid
            if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
                listPendingApplications(sender);
            } else if (args[0].equalsIgnoreCase("accept") && args.length == 2) {
                acceptApplication(sender, args[1]);
            } else {
                sender.sendMessage("Invalid command usage. Try /whitelist [list|accept <username>]");
            }
        }else{
            sender.sendMessage("No permission");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
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

    private void listPendingApplications(CommandSender sender) {
        String noApp = plugin.getConfig().getString("messages.no_applications", "No applications pending.");
        String appList = plugin.getConfig().getString("messages.application_list", "Pending applications:");
        Map<String, UUID> applications = whitelistManager.getPendingApplications();
        if (applications.isEmpty()) {
            sender.sendMessage(noApp);
        } else {
            sender.sendMessage(appList);
            for (String username : applications.keySet()) {
                sender.sendMessage("- " + username);
            }
        }
    }

    private void acceptApplication(CommandSender sender, String username) {
        String appAccept = plugin.getConfig().getString("messages.application_accepted", "Application accepted for {username}.");
        String noAppFound = plugin.getConfig().getString("messages.no_application_found", "No application found for {username}.");
        Map<String, UUID> applications = whitelistManager.getPendingApplications();
        if (applications.containsKey(username)) {
            whitelistManager.acceptApplication(username);
            sender.sendMessage(appAccept.replace("{username}", username));
        } else {
            sender.sendMessage(noAppFound.replace("{username}", username));
        }
    }
}

