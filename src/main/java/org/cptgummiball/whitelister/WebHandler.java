package org.cptgummiball.whitelister;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.bukkit.configuration.file.FileConfiguration;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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

        FileConfiguration messages = plugin.getLanguage().equalsIgnoreCase("de")
                ? plugin.getConfig("messages_de.yml") : plugin.getConfig("messages_en.yml");

        boolean useApi = plugin.getConfig().getBoolean("use_api", false);

        if (useApi) {
            handleApiRequests(target, request, response);
        } else {
            handleWebRequests(target, baseRequest, request, response);
        }
    }

    private void handleWebRequests(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // Set content type and response status
        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);  // This is still valid

        FileConfiguration messages = plugin.getLanguage().equalsIgnoreCase("de")
                ? plugin.getConfig("messages_de.yml") : plugin.getConfig("messages_en.yml");

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

            response.getWriter().println("<h1>" + messages.getString("server_rules_title") + "</h1>");

            // Check if a rules URL is set in the config
            String rulesUrl = plugin.getConfig().getString("rules_url", null);
            if (rulesUrl != null && !rulesUrl.isEmpty()) {
                // If a URL is set, display the hyperlink
                response.getWriter().println("<p><a href='" + rulesUrl + "' target='_blank'>" + messages.getString("server_rules_link_text") + "</a></p>");
            } else {
                // Otherwise, load and display rules from rules.yml
                FileConfiguration rulesConfig = plugin.getConfig("rules.yml");
                for (String rule : rulesConfig.getStringList("rules")) {
                    response.getWriter().println("<p>" + rule + "</p>");
                }
            }

            // Add the form for the application
            response.getWriter().println("<form action='/apply' method='post'>");
            response.getWriter().println("<label for='username'>Name:</label>");
            response.getWriter().println("<input type='text' id='username' name='username' placeholder='Your Minecraft Username' required><br>");
            response.getWriter().println("<label for='accept'>" + messages.getString("server_rules_accept") + ":</label>");
            response.getWriter().println("<input type='checkbox' id='accept' name='accept' required><br>");
            response.getWriter().println("<input type='submit' value='Submit'>");
            response.getWriter().println("</form>");

            response.getWriter().println("</div>");
            response.getWriter().println("</body></html>");

        } else if (target.equals("/apply") && "POST".equalsIgnoreCase(request.getMethod())) {
            String username = request.getParameter("username");
            String accept = request.getParameter("accept");

            if (accept != null && username != null) {
                whitelistManager.handleApplication(username);
                response.getWriter().println("<html><body><div class='container'><p>" + messages.getString("application_valid") + "</p></div></body></html>");
            } else {
                response.getWriter().println("<html><body><div class='container'><p>" + messages.getString("application_error") + "</p></div></body></html>");
            }
        }
    }

    private void handleApiRequests(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (target.equals("/api/apply") && "POST".equalsIgnoreCase(request.getMethod())) {
            String username = request.getParameter("username");
            String accept = request.getParameter("accept");

            response.setContentType("application/json");
            JSONObject jsonResponse = new JSONObject();
            if (accept != null && username != null) {
                if (whitelistManager.getUUIDFromUsername(username) != null) {
                    whitelistManager.handleApplication(username);
                    jsonResponse.put("status", "success");
                    jsonResponse.put("message", "Application successfully submitted.");
                }else if (whitelistManager.getUUIDFromUsername(username) == null){
                    jsonResponse.put("status", "UUIDerror");
                    jsonResponse.put("message", "UUID was not found.");
                } else {
                    jsonResponse.put("status", "error");
                    jsonResponse.put("message", "Please provide username and accept the rules.");
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