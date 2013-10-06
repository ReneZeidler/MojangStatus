package de.rene_zeidler.mojangstatus;

import java.io.File;

import net.craftminecraft.bungee.bungeeyaml.bukkitapi.InvalidConfigurationException;
import net.craftminecraft.bungee.bungeeyaml.supereasyconfig.Comment;
import net.craftminecraft.bungee.bungeeyaml.supereasyconfig.Config;
import net.md_5.bungee.api.plugin.Plugin;

public class MainConfig extends Config {
	public MainConfig(Plugin plugin) {
		this.CONFIG_FILE = new File(plugin.getDataFolder(), "config.yml");
		try {
			this.init();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	//Debugging enables a more detailed logging
	public boolean debug = false;
	@Comment(value = "All strings use & as the color code character\n\nInterval in which the status is checked (in seconds)")
	public int checkInterval = 30;
	@Comment(value = "MOTD messages, shown when the corresponding services are down. %motd% gets replaced with the standard MOTD\nSession servers are down, probably cannot join")
	public String sessionsDown = "&4Session-Server&c down -- leider möglicherweise &4kein Joinen&c möglich!";
	@Comment(value = "Session and login servers are down, probably cannot join or log in after restarting game")
	public String sessionsAndLoginDown = "&4Session- und Login-Server&c down -- leider möglicherweise &4kein Joinen&c möglich!";
	@Comment(value = "Login servers are down, can join but cannot log back in after restarting Minecraft")
	public String loginDown = "&4Login-Server&c down &r-- %motd%";
	@Comment(value = "The following messages are added additionally to the top ones. If you don't include %motd% they may overwrite the more important messages.\nSkin servers are down, skins may not be displayed correctly")
	public String skinsDown = "%motd%&c (&4Skin-Server&c down)";
	@Comment(value = "Minecraft.net is down, e.g. cannot access profile to change skin")
	public String minecraftNetDown = "%motd%&c (&4minecraft.net&c down)";
	@Comment(value = "Skin servers and minecraft.net are down")
	public String skinsAndMinecraftNetDown = "%motd%&c (&4Skin-Server und minecraft.net&c down)";
	
	@Comment(value = "\n\nOutput of the /mojangstatus command, %status% in status messages gets replaced with the color coded status of the service\nLine at the beginning of the output")
	public String commandCheckHeader = "&9Status der wichtigen Mojang-Dienste:";
	@Comment(value = "Status of the website minecraft.net")
	public String commandCheckStatusMinecraftNet = "&eDie Website &6minecraft.net&e ist gerade %status%";
	@Comment(value = "Status of the login servers (Yggdrasil)")
	public String commandCheckStatusAuthserverMojang = "&eDie &6Login-Server&e sind gerade %status%";
	@Comment(value = "Status of the session servers")
	public String commandCheckStatusSessionMinecraft = "&eDie &6Session-Server&e sind gerade %status%";
	@Comment(value = "Status of the skin servers")
	public String commandCheckStatusSkinsMinecraft = "&eDie &6Skin-Server&e sind gerade %status%";
	@Comment(value = "Warning when session servers are offline (no players can join)")
	public String commandCheckWarningSessionOffline = "&cEs können zur Zeit keine neuen Spieler joinen!";
	@Comment(value = "Warning when login servers are offline (only logged in players can join)")
	public String commandCheckWarningLoginOffline= "&cEs können zur Zeit nur Spieler joinen, die bereits in Minecraft eingeloggt sind!";
	@Comment(value = "Warning when skin servers are offline")
	public String commandCheckWarningSkinsOffline= "&cEs können zur Zeit Probleme mit den Skins auftretren!";
	@Comment(value = "Header for the second part of the output, less important services")
	public String commandCheckHeader2 = "&9Weitere Mojang-Dienste:";
	@Comment(value = "Status of the legacy mojang account servers")
	public String commandCheckStatusAuthMojang = "&fDie alten Mojang-Account &eLogin-Server&f sind gerade %status%";
	@Comment(value = "Status of the mojang account website")
	public String commandCheckStatusAccountMojang = "&fDie &eMojang-Account&f Website ist gerade %status%";
	@Comment(value = "Status of the legacy login servers")
	public String commandCheckStatusLoginMinecraft = "&fDie alten &eLogin-Server&f sind gerade %status%";
	
	@Comment(value = "\n\nPublic broadcasts when a service goes up or down. Use \n for multiple lines. Leave empty to disable.\nService went down\nAfter how many checks the broadcast should be displayed when a service goes down (0 = instantly, 2 = after the second check)? Avoids spamming the chat when the servers are just down for a short time")
	public int broadcastDownWait = 2;
	public String broadcastMinecraftNetDown = "";
	public String broadcastAuthserverMojangDown = "&cDie &lLogin-Server&c sind soeben &4offline&c gegangen.\n&cEs können sich vermutlich keine weiteren Spieler mehr in Minecraft einloggen!";
	public String broadcastSessionMinecraftDown = "&cDie &lSession-Server&c sind soeben &4offline&c gegangen.\n&cEs können vermutlich keine weiteren Spieler mehr joinen!";
	public String broadcastSkinsMinecraftDown = "";
	public String broadcastAuthMojangDown = "";
	public String broadcastAccountMojangDown = "";
	public String broadcastLoginMinecraftDown = "";
	@Comment(value = "Service goes up\nAfter how many checks the broadcast should be displayed when a service goes up?")
	public int broadcastUpWait = 1;
	public String broadcastMinecraftNetUp = "";
	@Comment(value = "Show broadcast that login servers are still down when session servers go up to avoid confusion")
	public boolean broadcastLoginStillDownOnSessionUp = true;
	public String broadcastAuthserverMojangUp = "&aDie &lLogin-Server&a sind soeben &2online&a gegangen.";
	@Comment(value = "Show broadcast that session servers are still down when login servers go up to avoid confusion")
	public boolean broadcastSessionStillDownOnLoginUp = true;
	public String broadcastSessionMinecraftUp = "&aDie &lSession-Server&a sind soeben &2online&a gegangen.\n&aEs können wieder weitere Spieler joinen!";
	public String broadcastSkinsMinecraftUp = "";
	public String broadcastAuthMojangUp = "";
	public String broadcastAccountMojangUp = "";
	public String broadcastLoginMinecraftUp = "";
	@Comment(value = "Public broadcasts when a service is still down after a certain amount of time.\nNumber of continuous checks where the service is offline until the message gets shown (e.g. with check interval 30s a value of 20 means a broadcast after 10 minutes)")
	public int remainsDownInterval = 20;
	public String broadcastMinecraftNetStillDown = "";
	public String broadcastAuthserverMojangStillDown = "&cDie &lLogin-Server&c sind immer noch &4offline&c, es können sich keine weiteren Spieler in Minecraft einloggen!";
	public String broadcastSessionMinecraftStillDown = "&cDie &lSession-Server&c sind immer noch &4offline&c und es können keine weiteren Spieler joinen!";
	public String broadcastSkinsMinecraftStillDown = "";
	public String broadcastAuthMojangStillDown = "";
	public String broadcastAccountMojangStillDown = "";
	public String broadcastLoginMinecraftStillDown = "";
}