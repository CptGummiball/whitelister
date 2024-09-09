package org.cptgummiball.whitelister;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class WhitelistManager {
    private final Whitelister plugin;
    private File applicationFile;
    private FileConfiguration applicationConfig;

    public WhitelistManager(Whitelister plugin) {
        this.plugin = plugin;
        setupFiles();
    }

    private void setupFiles() {
        applicationFile = new File(plugin.getDataFolder(), "applications.yml");
        if (!applicationFile.exists()) {
            applicationFile.getParentFile().mkdirs();
            plugin.saveResource("applications.yml", false);
        }
        applicationConfig = YamlConfiguration.loadConfiguration(applicationFile);
    }

    public void handleApplication(String username) {
        // Fetch UUID from Mojang API
        UUID uuid = fetchUUID(username);
        if (uuid != null) {
            applicationConfig.set("applications." + username + ".uuid", uuid.toString());
            saveApplications();
        }
    }

    public void saveApplications() {
        try {
            applicationConfig.save(applicationFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private UUID fetchUUID(String username) {
        // Mojang API request to fetch UUID
        try {
            java.net.URL url = new java.net.URL("https://api.mojang.com/users/profiles/minecraft/" + username);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            conn.disconnect();

            // Parse JSON
            org.json.JSONObject obj = new org.json.JSONObject(content.toString());
            return UUID.fromString(obj.getString("id").replaceFirst(
                    "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public FileConfiguration getApplicationConfig() {
        return applicationConfig;
    }

    public void deleteApplication(String username) {
        applicationConfig.set("applications." + username, null);
        saveApplications();
    }
}