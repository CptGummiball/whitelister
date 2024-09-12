# Whitelister Plugin
![Spigot](https://img.shields.io/badge/Spigot-1.20.x-yellow.svg)
![MIT License](https://img.shields.io/badge/PaperMC-1.20.x-blue.svg)
![Version](https://img.shields.io/badge/Version-1.0.0-gray.svg)
![MIT License](https://img.shields.io/badge/License-MIT-green.svg)

**Author:** CptGummiball

## Overview
Whitelister is a Spigot plugin designed to manage server whitelist applications via a web interface. The plugin sets up a simple website using Jetty, where players can review the server rules and submit a whitelist application. Server administrators can then manage these applications directly in-game with intuitive commands. There is also an API for usage with own front end.

## Key Features
Web-based Whitelist Application: Players can apply for whitelist access via a simple web page where they must agree to the server rules before submitting their application.
Customizable Rules: Server rules can be defined either in a rules.yml file or linked to an external URL, both of which are displayed on the web page.
Mojang API Integration: Automatically fetches and records the UUID of players applying for whitelist access.
In-game Whitelist Management: Administrators can review, accept, or reject applications in-game via commands.
Multi-language Support: in the ``config.yml`` you can change the text output.

## Setup and Configuration
### Installation:

Download the plugin JAR and place it in your server's plugins folder.
Start the server to generate the default configuration files.
Stop the server to adjust the configuration files as needed.

### Configuration Files:

**config.yml:**

Example configuration:
````yaml
# Webserver Configuration
port: 8013

# Enable or disable notifications for new applications
notifications:
  enabled: true

# Using the API instead of internal frontend
use_api: false

# Optional URL for server rules
# If set, the URL will be displayed as a hyperlink instead of showing the rules from rules.yml
rules_url: ""

# Translation
messages:
  no_applications: "No applications found."
  application_list: "Application List:"
  application_accepted: "Application for {username} accepted"
  no_application_found: "No application for {username}"
  application_valid: "Username valid, request send!"
  application_error: "Error: Please provide a valid username."
  application_title: "Whitelist Application"
  server_rules_accept: "Please accept the rules to submit your application."
  pending_applications_notification: "There are pending whitelist applications."
  server_rules_link_text: "Click here to read the server rules."
````

**rules.yml:**

````yaml
# Rules configuration
# Define your server rules here. Each rule should be a list item (-).
# These rules will be displayed on the web interface.
rules:
  - "No griefing."
  - "Be respectful to other players."
  - "No hacking or cheating."
  - "Follow the instructions of the server admins."

# Example:
# rules:
#   - "Your first rule."
#   - "Your second rule."
````

**messages.yml:**

This file stores all language-specific messages. You can customize texts like the server rules title, application success message, etc.

### Starting the Web Server:

The plugin automatically starts a Jetty web server on the configured port (default: 8013). You can access the application form at http://<your-server-ip>:<port>/.

### Commands and Permissions
**Commands:**

``/whitelister list``
Displays all pending whitelist applications.

``/whitelister accept <username>``
Accepts a player's whitelist application and adds them to the server's whitelist.

**Permissions:**

``whitelister.manage``
Grants access to the /whitelister commands for managing applications. Players with this permission will be notified of pending applications when they log in.

## How It Works

### Player Application:

Players navigate to the web page served by Jetty.
They review the server rules (either from rules.yml or a linked URL).
They submit their Minecraft username and agree to the rules.

### Application Processing:

The player's username and UUID (fetched from the Mojang API) are stored in a json file.
Administrators with the whitelister.manage permission can view pending applications in-game and accept them using the /whitelister accept command.
Whitelist Update:

Upon accepting an application, the plugin adds the player to the server's whitelist and moves their application data to an "accepted" YAML file for record-keeping.

## API Usage
The API can be used to manage requests via HTTP requests. Make sure the use_api option in the config.yml is set to true to turn off the internal web server and use the API.

**Base URL:** ``http://<server-ip>:<port>/api/``

**Submit application**
- Endpoint: /api/apply
- Method: POST
- Parameter:
username (string): The Minecraft Username.
accept (string): Must be set to "on" to accept the rules.

Sample request:
````bash
curl -X POST http://<server-ip>:<port>/api/apply \
     -d "username=player123" \
     -d "accept=on"
````
Answer:
````json
{
  "status": "success",
  "message": "Application successfully submitted."
}
````
In case of errors:

````json
{
  "status": "error",
  "message": "Please provide username and accept the rules."
}
````
Or:
````json
{
  "status": "error",
  "message": "UUID not found."
}
````

**Retrieve all applications**
- Endpoint: /api/requests
- Method: GET

Sample request:

````bash
curl http://<server-ip>:<port>/api/requests
````
Answer:
````json
{
  "player123": "uuid-1",
  "player456": "uuid-2"
}
````

## License
This plugin is open-source and distributed under the [MIT License](LICENSE).
