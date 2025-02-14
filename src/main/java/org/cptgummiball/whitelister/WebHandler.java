package org.cptgummiball.whitelister;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.Callback;
import org.json.JSONObject;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        httpResponse.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);

        boolean useApi = plugin.getConfig().getBoolean("use_api", false);

        if (useApi) {
            handleApiRequests(request.getHttpURI().getPath(), request, response);
            plugin.getLogger().info("Using API");
        } else {
            handleWebRequests(request.getHttpURI().getPath(), (HttpServletRequest) request, (HttpServletResponse) response);
            plugin.getLogger().info("Using Web Front-End");
        }

        callback.succeeded();
        return true;
    }

    private void handleWebRequests(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);

        if (target.equals("/")) {
            response.getWriter().println("<html><head>");
            response.getWriter().println("<style>");
            response.getWriter().println("body { font-family: Arial, sans-serif; background-color: #f4f4f4; color: #333; }");
            response.getWriter().println("h1 { text-align: center; color: #444; }");
            response.getWriter().println(".container { max-width: 600px; margin: 50px auto; padding: 20px; background-color: white; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }");
            response.getWriter().println("</style>");
            response.getWriter().println("</head><body>");
            response.getWriter().println("<div class='container'>");
            response.getWriter().println("<h1>" + plugin.getConfig().getString("messages.application_title", "") + "</h1>");

            String rulesUrl = plugin.getConfig().getString("rules_url", null);
            String linkText = plugin.getConfig().getString("messages.server_rules_link_text", null);
            String rulesAccept = plugin.getConfig().getString("messages.server_rules_accept", null);
            if (rulesUrl != null && !rulesUrl.isEmpty()) {
                response.getWriter().println("<p><a href='" + rulesUrl + "' target='_blank'>" + linkText + "</a></p>");
            } else {
                String rule = String.valueOf(plugin.getConfig().getStringList("rules"));
                response.getWriter().println("<p>" + rule + "</p>");
            }

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
                } else {
                    whitelistManager.handleApplication(username);
                    response.getWriter().println("<html><body><div class='container'><p>" + valid + "</p></div></body></html>");
                }
            } else {
                response.getWriter().println("<html><body><div class='container'><p>" + error + "</p></div></body></html>");
            }
        }
    }

    private void handleApiRequests(String target, Request request, Response response) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        httpResponse.setContentType("application/json");
        JSONObject jsonResponse = new JSONObject();

        if (target.equals("/api/apply") && "POST".equalsIgnoreCase(request.getMethod())) {
            String username = httpRequest.getParameter("username");
            String accept = httpRequest.getParameter("accept");

            if (accept != null && username != null) {
                if (whitelistManager.isUserOnWhitelist(username)) {
                    jsonResponse.put("status", "whitelistError");
                    jsonResponse.put("message", "User already on whitelist");
                } else {
                    UUID uuid = whitelistManager.getUUIDFromUsername(username);
                    if (uuid != null) {
                        whitelistManager.handleApplication(username);
                        jsonResponse.put("status", "success");
                        jsonResponse.put("message", "Application successfully submitted.");
                    } else {
                        jsonResponse.put("status", "UUIDerror");
                        jsonResponse.put("message", "UUID was not found.");
                    }
                }
            } else {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Invalid request parameters.");
            }
            httpResponse.getWriter().write(jsonResponse.toString());

        } else if (target.equals("/api/requests") && "GET".equalsIgnoreCase(request.getMethod())) {
            String json = whitelistManager.getPendingRequestsAsJson();
            httpResponse.getWriter().write(json);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
