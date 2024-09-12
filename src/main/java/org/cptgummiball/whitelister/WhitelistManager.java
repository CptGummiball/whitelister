package org.cptgummiball.whitelister;

import org.bukkit.configuration.file.FileConfiguration;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WhitelistManager {
    private final Whitelister plugin;
    private final Map<String, UUID> pendingApplications = new ConcurrentHashMap<>();
    private final File applicationsFile;

    public WhitelistManager(Whitelister plugin) {
        this.plugin = plugin;
        this.applicationsFile = new File(plugin.getDataFolder(), "pending_applications.json");
        loadPendingApplications();
    }

    public void handleApplication(String username) {
        UUID uuid = getUUIDFromUsername(username);
        if (uuid != null) {
            pendingApplications.put(username, uuid);
            savePendingApplications();
        }
    }

    public String getPendingRequestsAsJson() {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, UUID> entry : pendingApplications.entrySet()) {
            json.put(entry.getKey(), entry.getValue().toString());
        }
        return json.toString();
    }

    public void acceptApplication(String username) {
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "whitelist add " + username);
        UUID uuid = pendingApplications.remove(username);
        if (uuid != null) {
            savePendingApplications();
        }
    }

    UUID getUUIDFromUsername(String username) {
        return fetchUUID(username);
    }

    private void savePendingApplications() {
        try (FileWriter fileWriter = new FileWriter(applicationsFile)) {
            JSONObject json = new JSONObject();
            for (Map.Entry<String, UUID> entry : pendingApplications.entrySet()) {
                json.put(entry.getKey(), entry.getValue().toString());
            }
            fileWriter.write(json.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPendingApplications() {
        if (applicationsFile.exists()) {
            try {
                String jsonContent = new String(java.nio.file.Files.readAllBytes(applicationsFile.toPath()));
                JSONObject json = new JSONObject(jsonContent);
                for (String key : json.keySet()) {
                    UUID uuid = UUID.fromString(json.getString(key));
                    pendingApplications.put(key, uuid);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    public Map<String, UUID> getPendingApplications() {
        return new HashMap<>(pendingApplications);

    }
}