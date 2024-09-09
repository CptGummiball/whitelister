# Whitelister Plugin
![Spigot](https://img.shields.io/badge/Spigot-1.20.x-yellow.svg)
![Version](https://img.shields.io/badge/Version-1.0.0-gray.svg)
![MIT License](https://img.shields.io/badge/License-MIT-green.svg)

**Author:** CptGummiball

## Overview
Whitelister is a Spigot plugin designed to manage server whitelist applications via a web interface. The plugin sets up a simple website using Jetty, where players can review the server rules and submit a whitelist application. Server administrators can then manage these applications directly in-game with intuitive commands.

## Key Features
Web-based Whitelist Application: Players can apply for whitelist access via a simple web page where they must agree to the server rules before submitting their application.
Customizable Rules: Server rules can be defined either in a rules.yml file or linked to an external URL, both of which are displayed on the web page.
Mojang API Integration: Automatically fetches and records the UUID of players applying for whitelist access.
In-game Whitelist Management: Administrators can review, accept, or reject applications in-game via commands.
Multi-language Support: English and German languages are supported, and administrators can configure which language to use in the config.yml file.

## Setup and Configuration
### Installation:

Download the plugin JAR and place it in your server's plugins folder.
Start the server to generate the default configuration files.
Stop the server to adjust the configuration files as needed.

### Configuration Files:

**config.yml:**

Set the webserver port and specify whether to use a rules URL or a local rules.yml file.
Example configuration:
````yaml
# Webserver Configuration
port: 8080

# Language options (en/de)
language: en

# Enable or disable notifications for new applications
notifications:
  enabled: true

# Optional URL for server rules
# If set, the URL will be displayed as a hyperlink instead of showing the rules from rules.yml
rules_url: "https://example.com/server-rules"
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

**messages_en.yml and messages_de.yml:**

These files store all language-specific messages. You can customize texts like the server rules title, application success message, etc.

### Starting the Web Server:

The plugin automatically starts a Jetty web server on the configured port (default: 8080). You can access the application form at http://<your-server-ip>:<port>/.

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

The player's username and UUID (fetched from the Mojang API) are stored in a YAML file.
Administrators with the whitelister.manage permission can view pending applications in-game and accept them using the /whitelister accept command.
Whitelist Update:

Upon accepting an application, the plugin adds the player to the server's whitelist and moves their application data to an "accepted" YAML file for record-keeping.

## License
This plugin is open-source and distributed under the [MIT License](LICENSE).
