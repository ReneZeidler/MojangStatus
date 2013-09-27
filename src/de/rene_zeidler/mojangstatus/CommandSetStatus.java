package de.rene_zeidler.mojangstatus;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CommandSetStatus extends Command {
	
	public CommandSetStatus()
	{
		super("setstatus", "mojangstatus.set", "setmcs");
	}

	@Override
	public void execute(CommandSender sender, String[] args)
	{
		if(args.length != 2) {
			sender.sendMessage(ChatColor.RED  + "Wrong number of arguments. Use /setstatus <server> <online|offline>");
			return;
		}
		
		//Parse second argument (online/offline)
		boolean online = false;	//To what to set the status on (true is online, false is offline)
		if(args[1].equalsIgnoreCase("online") || args[1].equalsIgnoreCase("true")) online = true;
		else if(args[1].equalsIgnoreCase("offline") || args[1].equalsIgnoreCase("false")) online = false;
		else {
			sender.sendMessage(ChatColor.RED  + "Second argument must be \"online\" or \"offline\"");
			return;
		}
		
		//Parse the first argument and set the status of the service
		if(args[0].equalsIgnoreCase("minecraftNet") || args[0].equalsIgnoreCase("website") || args[0].equalsIgnoreCase("minecraft.net")) MojangStatus.getInstance().setMinecraftNetStatus(online);
		else if(args[0].equalsIgnoreCase("authServerMojang") || args[0].equalsIgnoreCase("login") || args[0].equalsIgnoreCase("authserver.mojang.com")) MojangStatus.getInstance().setAuthServerMojangStatus(online);
		else if(args[0].equalsIgnoreCase("sessionMinecraft") || args[0].equalsIgnoreCase("session") || args[0].equalsIgnoreCase("session.minecraft.net")) MojangStatus.getInstance().setSessionMinecraftStatus(online);
		else if(args[0].equalsIgnoreCase("skinsMinecraft") || args[0].equalsIgnoreCase("skins") || args[0].equalsIgnoreCase("skins.minecraft.net")) MojangStatus.getInstance().setSkinsMinecraftStatus(online);
		else if(args[0].equalsIgnoreCase("authMojang") || args[0].equalsIgnoreCase("auth") || args[0].equalsIgnoreCase("auth.mojang.com")) MojangStatus.getInstance().setAuthMojangStatus(online);
		else if(args[0].equalsIgnoreCase("accountMojang") || args[0].equalsIgnoreCase("account") || args[0].equalsIgnoreCase("account.mojang.com")) MojangStatus.getInstance().setAccountMojangStatus(online);
		else if(args[0].equalsIgnoreCase("loginMinecraft") || args[0].equalsIgnoreCase("legacylogin") || args[0].equalsIgnoreCase("login.minecraft.net")) MojangStatus.getInstance().setLoginMinecraftStatus(online);
		else {
			sender.sendMessage(ChatColor.RED  + "Unknown server, please use the server address or the service name");
			return;
		}
		sender.sendMessage(ChatColor.GREEN  + "Successfully set status of " + ChatColor.GOLD + args[0] + ChatColor.GREEN + " to " + (online ? (ChatColor.DARK_GREEN + "online") : (ChatColor.DARK_RED + "offline")));
	}
	
}