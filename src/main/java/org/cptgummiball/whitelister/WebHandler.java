package org.cptgummiball.whitelister;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.bukkit.configuration.file.FileConfiguration;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

public class WebHandler extends AbstractHandler {
    private final WhitelistManager whitelistManager;
    private final Whitelister plugin;

    public WebHandler(WhitelistManager whitelistManager, Whitelister plugin) {
        this.whitelistManager = whitelistManager;
        this.plugin = plugin;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        boolean useApi = plugin.getConfig().getBoolean("use_api", false);

        if (useApi == true) {
            handleApiRequests(target, request, response);
            plugin.getLogger().info("Using API");
        } else {
            handleWebRequests(target, baseRequest, request, response);
            plugin.getLogger().info("Using Web Front-End");
        }
    }

    private void handleWebRequests(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // Set content type and response status
        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);  // This is still valid

        if (target.equals("/")) {
            response.getWriter().println("<html><head>");
            response.getWriter().println("<style>");
            response.getWriter().println("body { font-family: Arial, sans-serif; background-color: #f4f4f4; color: #333; }");
            response.getWriter().println("h1 { text-align: center; color: #444; }");
            response.getWriter().println(".container { max-width: 600px; margin: 50px auto; padding: 20px; background-color: white; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }");
            response.getWriter().println("p { font-size: 16px; line-height: 1.5; margin-bottom: 20px; }");
            response.getWriter().println("a { color: #3498db; text-decoration: none; }");
            response.getWriter().println("a:hover { text-decoration: underline; }");
            response.getWriter().println("form { display: flex; flex-direction: column; align-items: center; }");
            response.getWriter().println("input[type='text'], input[type='checkbox'] { padding: 10px; margin-bottom: 20px; font-size: 16px; width: 80%; border: 1px solid #ccc; border-radius: 4px; }");
            response.getWriter().println("input[type='submit'] { padding: 10px 20px; background-color: #3498db; color: white; font-size: 16px; border: none; border-radius: 4px; cursor: pointer; }");
            response.getWriter().println("input[type='submit']:hover { background-color: #2980b9; }");
            response.getWriter().println("</style>");
            response.getWriter().println("</head><body>");
            response.getWriter().println("<div class='container'>");

            response.getWriter().println("<h1>" + plugin.getConfig().getString("messages.application_title", null) + "</h1>");

            // Check if a rules URL is set in the config
            String rulesUrl = plugin.getConfig().getString("rules_url", null);
            String linkText = plugin.getConfig().getString("messages.server_rules_link_text", null);
            String rulesAccept = plugin.getConfig().getString("messages.server_rules_accept", null);
            if (rulesUrl != null && !rulesUrl.isEmpty()) {
                // If a URL is set, display the hyperlink
                response.getWriter().println("<p><a href='" + rulesUrl + "' target='_blank'>" + linkText + "</a></p>");
            } else {
                // Otherwise, load and display rules from config.yml
                String rule = String.valueOf(plugin.getConfig().getStringList("rules"));
                response.getWriter().println("<p>" + rule + "</p>");
            }

            // Add the form for the application
            response.getWriter().println("<form action='/apply' method='post'>");
            response.getWriter().println("<label for='username'>Name:</label>");
            response.getWriter().println("<input type='text' id='username' name='username' placeholder='Minecraft Username' required><br>");
            response.getWriter().println("<label for='accept'>" + rulesAccept + ":</label>");
            response.getWriter().println("<input type='checkbox' id='accept' name='accept' required><br>");
            response.getWriter().println("<input type='submit' value='Submit'>");
            response.getWriter().println("</form>");

            response.getWriter().println("</div>");
            response.getWriter().println("</body></html>");

        } else if (target.equals("/apply") && "POST".equalsIgnoreCase(request.getMethod())) {
            String username = request.getParameter("username");
            String accept = request.getParameter("accept");
            String valid = plugin.getConfig().getString("messages.application_valid", null);
            String error = plugin.getConfig().getString("messages.application_error", null);
            String whitelist = plugin.getConfig().getString("messages.application_isonwhitelist", null);

            if (accept != null && username != null) {
                if (whitelistManager.isUserOnWhitelist(username)) {
                    response.getWriter().println("<html><body><div class='container'><p>" + whitelist + "</p></div></body></html>");
                }else {
                    whitelistManager.handleApplication(username);
                    response.getWriter().println("<html><body><div class='container'><p>" + valid + "</p></div></body></html>");
                }
            } else {
                response.getWriter().println("<html><body><div class='container'><p>" + error + "</p></div></body></html>");
            }
        }
    }

    private void handleApiRequests(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (target.equals("/api/apply") && "POST".equalsIgnoreCase(request.getMethod())) {
            String username = request.getParameter("username");
            String accept = request.getParameter("accept");

            response.setContentType("application/json");
            JSONObject jsonResponse = new JSONObject();

            // Verify that the parameters are accepted and the username exists
            if (accept != null && username != null) {
                // Check if the user is already whitelisted
                if (whitelistManager.isUserOnWhitelist(username)) {
                    jsonResponse.put("status", "whitelistError");
                    jsonResponse.put("message", "User already on whitelist");
                } else {
                    // If the user is not whitelisted, we check the UUID
                    UUID uuid = whitelistManager.getUUIDFromUsername(username);
                    if (uuid != null) {
                        // Here the application is processed because the user is not on the whitelist and has a valid UUID
                        whitelistManager.handleApplication(username);
                        jsonResponse.put("status", "success");
                        jsonResponse.put("message", "Application successfully submitted.");
                    } else {
                        jsonResponse.put("status", "UUIDerror");
                        jsonResponse.put("message", "UUID was not found.");
                    }
                }
                response.getWriter().write(jsonResponse.toString());
            } else if (target.equals("/api/requests") && "GET".equalsIgnoreCase(request.getMethod())) {
                response.setContentType("application/json");
                String json = whitelistManager.getPendingRequestsAsJson();
                response.getWriter().write(json);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }

}