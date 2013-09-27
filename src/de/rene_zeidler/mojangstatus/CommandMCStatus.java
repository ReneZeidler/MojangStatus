package de.rene_zeidler.mojangstatus;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CommandMCStatus extends Command {
	MainConfig config;
	
	public CommandMCStatus()
	{
		super("mojangstatus", "mojangstatus.check", "mcstatus", "mcs");
		config = MojangStatus.getInstance().getConfig();
	}

	@Override
	public void execute(CommandSender sender, String[] args)
	{
		//Just parse the messages from the config and send it one after another
		sender.sendMessage(parseMessage(config.commandCheckHeader));
		sender.sendMessage(parseMessage(config.commandCheckStatusMinecraftNet, MojangStatus.minecraftNet, false));
		sender.sendMessage(parseMessage(config.commandCheckStatusAuthserverMojang, MojangStatus.authserverMojang, false));
		sender.sendMessage(parseMessage(config.commandCheckStatusSessionMinecraft, MojangStatus.sessionMinecraft, false));
		sender.sendMessage(parseMessage(config.commandCheckStatusSkinsMinecraft, MojangStatus.skinsMinecraft, false));
		if(!MojangStatus.sessionMinecraft) sender.sendMessage(parseMessage(config.commandCheckWarningSessionOffline));		//Warning when session servers are offline
		else if(!MojangStatus.authserverMojang) sender.sendMessage(parseMessage(config.commandCheckWarningLoginOffline));	//Warning when login servers are offline
		if(!MojangStatus.skinsMinecraft) sender.sendMessage(parseMessage(config.commandCheckWarningSkinsOffline));			//Warning when skin servers are offline
		sender.sendMessage(parseMessage(config.commandCheckHeader2));
		sender.sendMessage(parseMessage(config.commandCheckStatusAuthMojang, MojangStatus.authMojang, true));
		sender.sendMessage(parseMessage(config.commandCheckStatusAccountMojang, MojangStatus.accountMojang, true));
		sender.sendMessage(parseMessage(config.commandCheckStatusLoginMinecraft, MojangStatus.loginMinecraft, true));
	}
	
	/**
	 * Parses a message from the config (adds color codes)
	 * @param m message to parse
	 * @return parsed message
	 */
	private String parseMessage(String m)
	{
		return ChatColor.translateAlternateColorCodes('&', m);
				
	}
	
	/**
	 * Parses an online/offline message
	 * Replaces %status% with the status of the service in green or red (or dark green and dark red when light is set to false)
	 * @param m message to parse
	 * @param online online status of the service
	 * @param light use lighter colors
	 * @return parsed message
	 */
	private String parseMessage(String m, boolean online, boolean light)
	{
		return ChatColor.translateAlternateColorCodes('&', m.replace("%status%",
				(online ? ((light ? ChatColor.GREEN : ChatColor.DARK_GREEN) + "online")
						: ((light ? ChatColor.RED : ChatColor.DARK_RED) + "offline"))));
				
	}
	
}
