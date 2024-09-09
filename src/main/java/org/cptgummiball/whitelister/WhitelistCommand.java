package org.cptgummiball.whitelister;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class WhitelistCommand implements CommandExecutor, TabCompleter {
    private final Whitelister plugin;

    public WhitelistCommand(Whitelister plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Existing command logic for list and accept
        FileConfiguration messages = plugin.getLanguage().equalsIgnoreCase("de")
                ? plugin.getConfig("messages_de.yml") : plugin.getConfig("messages_en.yml");

        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            FileConfiguration config = plugin.getWhitelistManager().getApplicationConfig();
            if (config.getConfigurationSection("applications") == null) {
                sender.sendMessage(ChatColor.RED + messages.getString("no_applications"));
                return true;
            }
            sender.sendMessage(ChatColor.GOLD + messages.getString("application_list"));
            config.getConfigurationSection("applications").getKeys(false).forEach(name -> {
                String uuid = config.getString("applications." + name + ".uuid");
                sender.sendMessage(ChatColor.GREEN + name + " - " + uuid);
            });
        } else if (args[0].equalsIgnoreCase("accept") && args.length == 2) {
            String username = args[1];
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "whitelist add " + username);
            plugin.getWhitelistManager().deleteApplication(username);
            sender.sendMessage(ChatColor.GREEN + messages.getString("application_accepted").replace("%player%", username));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        FileConfiguration config = plugin.getWhitelistManager().getApplicationConfig();

        if (args.length == 2 && args[0].equalsIgnoreCase("accept")) {
            if (config.getConfigurationSection("applications") != null) {
                List<String> names = new ArrayList<>(config.getConfigurationSection("applications").getKeys(false));
                StringUtil.copyPartialMatches(args[1], names, completions);
            }
        }
        return completions;
    }
}