package org.cptgummiball.whitelister;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.eclipse.jetty.server.Server;

public class Whitelister extends JavaPlugin {
    private Server webServer;
    private WhitelistManager whitelistManager;

    @Override
    public void onEnable() {
        // Load configuration
        saveDefaultConfig();
        whitelistManager = new WhitelistManager(this);

        // Start Jetty server on configured port
        startWebServer();

        // Register commands and tab completer
        WhitelistCommand whitelistCommand = new WhitelistCommand(this, this.getWhitelistManager());
        this.getCommand("whitelister").setExecutor(whitelistCommand);
        this.getCommand("whitelister").setTabCompleter(whitelistCommand); // Register tab completer

        // Register event listener for player join notifications
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);


        getLogger().info("Whitelister Plugin enabled!");
    }

    @Override
    public void onDisable() {
        try {
            if (webServer != null) {
                webServer.stop();
            }
        } catch (Exception e) {
            getLogger().info("");
        }
        getLogger().info("Whitelister Plugin disabled!");
    }

    private void startWebServer() {
        int port = getConfig().getInt("port", 8013); // Get port from config
        webServer = new Server(port);
        webServer.setHandler(new WebHandler(whitelistManager, this));
        try {
            webServer.start();
        } catch (Exception e) {
            getLogger().info("Webserver failure");
        }
    }

    public WhitelistManager getWhitelistManager() {
        return whitelistManager;
    }

    public FileConfiguration getConfig(String string) {
        return getConfig();
    }
}

