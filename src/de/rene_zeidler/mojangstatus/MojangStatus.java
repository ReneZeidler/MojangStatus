package de.rene_zeidler.mojangstatus;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import net.craftminecraft.bungee.bungeeyaml.bukkitapi.InvalidConfigurationException;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.event.EventHandler;

public class MojangStatus extends Plugin implements Listener {
	public static boolean minecraftNet = true;
	public static boolean loginMinecraft = true;
	public static boolean sessionMinecraft = true;
	public static boolean accountMojang = true;
	public static boolean authMojang = true;
	public static boolean skinsMinecraft = true;
	public static boolean authserverMojang = true;
	public int minecraftNetDowntimer = 0;
	public int loginMinecraftDowntimer = 0;
	public int sessionMinecraftDowntimer = 0;
	public int accountMojangDowntimer = 0;
	public int authMojangDowntimer = 0;
	public int skinsMinecraftDowntimer = 0;
	public int authserverMojangDowntimer = 0;
	private MainConfig config;
	private ScheduledTask task;
	public int index = 0;
	private static MojangStatus instance = null;
	
	public static MojangStatus getInstance()
	{
		if(instance == null) instance = (MojangStatus) BungeeCord.getInstance().getPluginManager().getPlugin("MojangStatus");
		return instance;
	}
	
	public MainConfig getConfig()
	{
		return config;
	}
	
	public void onEnable()
	{
		this.config = new MainConfig(this);
		BungeeCord.getInstance().getPluginManager().registerListener(this, this);
		BungeeCord.getInstance().getPluginManager().registerCommand(this, new CommandMCStatus());
		BungeeCord.getInstance().getPluginManager().registerCommand(this, new CommandSetStatus());
		task = BungeeCord.getInstance().getScheduler().schedule(this, new StatusChecker(), 0, config.checkInterval, TimeUnit.SECONDS);
		getLogger().log(Level.INFO, "Initialized Scheduler with an interval of " + config.checkInterval + " seconds");
		if(config.debug) getLogger().log(Level.INFO, "Debugging enabled");
	}

	
	public void onDisable()
	{
		try {
			this.config.save();
		} catch (InvalidConfigurationException e) {
			getLogger().log(Level.SEVERE, "Error while saving the config:");
			e.printStackTrace();
		}
		task.cancel();
	}
	
	public void setMinecraftNetStatus(boolean online)
	{
		if(minecraftNet != online) {
			getLogger().log(Level.INFO, "Minecraft.net just went " + (online ? "online" : "offline"));
			if(online) {
				minecraftNetDowntimer = 0;
				broadcast(config.broadcastMinecraftNetUp);
			}
			else broadcast(config.broadcastMinecraftNetDown);
		} else if(!online) {
			minecraftNetDowntimer++;
			if(minecraftNetDowntimer >= config.remainsDownInterval) {
				broadcast(config.broadcastMinecraftNetStillDown);
				minecraftNetDowntimer = 0;
			}
		}
		minecraftNet = online;
	}
	
	public void setLoginMinecraftStatus(boolean online)
	{
		if(loginMinecraft != online) {
			getLogger().log(Level.INFO, "Login servers (legacy) just went " + (online ? "online" : "offline"));
			if(online) {
				loginMinecraftDowntimer = 0;
				broadcast(config.broadcastLoginMinecraftUp);
			}
			else broadcast(config.broadcastLoginMinecraftDown);
		} else if(!online) {
			loginMinecraftDowntimer++;
			if(loginMinecraftDowntimer >= config.remainsDownInterval) {
				broadcast(config.broadcastLoginMinecraftStillDown);
				loginMinecraftDowntimer = 0;
			}
		}
		loginMinecraft = online;
	}
	
	public void setSessionMinecraftStatus(boolean online)
	{
		if(sessionMinecraft != online) {
			getLogger().log(Level.INFO, "Session servers (legacy) just went " + (online ? "online" : "offline"));
			if(online) {
				sessionMinecraftDowntimer = 0;
				broadcast(config.broadcastSessionMinecraftUp);
				if(!authserverMojang && config.broadcastLoginStillDownOnSessionUp) broadcast(config.broadcastAuthserverMojangStillDown);
			}
			else broadcast(config.broadcastSessionMinecraftDown);
		} else if(!online) {
			sessionMinecraftDowntimer++;
			if(sessionMinecraftDowntimer >= config.remainsDownInterval) {
				broadcast(config.broadcastSessionMinecraftStillDown);
				sessionMinecraftDowntimer = 0;
			}
		} 
		sessionMinecraft = online;
	}
	
	public void setAccountMojangStatus(boolean online)
	{
		if(accountMojang != online) {
			getLogger().log(Level.INFO, "Mojang accounts website just went " + (online ? "online" : "offline"));
			if(online) {
				accountMojangDowntimer = 0;
				broadcast(config.broadcastAccountMojangUp);
			}
			else broadcast(config.broadcastAccountMojangDown);
		} else if(!online) {
			accountMojangDowntimer++;
			if(accountMojangDowntimer >= config.remainsDownInterval) {
				broadcast(config.broadcastAccountMojangStillDown);
				accountMojangDowntimer = 0;
			}
		}
		accountMojang = online;
	}
	
	public void setAuthMojangStatus(boolean online)
	{
		if(authMojang != online) {
			getLogger().log(Level.INFO, "Mojang accounts login (legacy) just went " + (online ? "online" : "offline"));
			if(online) {
				authMojangDowntimer = 0;
				broadcast(config.broadcastAuthMojangUp);
			}
			else broadcast(config.broadcastAuthMojangDown);
		} else if(!online) {
			authMojangDowntimer++;
			if(authMojangDowntimer >= config.remainsDownInterval) {
				broadcast(config.broadcastAuthMojangStillDown);
				authMojangDowntimer = 0;
			}
		} 
		authMojang = online;
	}
	
	public void setSkinsMinecraftStatus(boolean online)
	{
		if(skinsMinecraft != online) {
			getLogger().log(Level.INFO, "Skin servers just went " + (online ? "online" : "offline"));
			if(online) {
				skinsMinecraftDowntimer = 0;
				broadcast(config.broadcastSkinsMinecraftUp);
			}
			else broadcast(config.broadcastSkinsMinecraftDown);
		} else if(!online) {
			skinsMinecraftDowntimer++;
			if(skinsMinecraftDowntimer >= config.remainsDownInterval) {
				broadcast(config.broadcastSkinsMinecraftStillDown);
				skinsMinecraftDowntimer = 0;
			}
		}
		skinsMinecraft = online;
	}
	
	public void setAuthServerMojangStatus(boolean online)
	{
		if(authserverMojang != online) {
			getLogger().log(Level.INFO, "Authentification service (Minecraft login) just went " + (online ? "online" : "offline"));
			if(online) {
				authserverMojangDowntimer = 0;
				broadcast(config.broadcastAuthserverMojangUp);
				if(!sessionMinecraft && config.broadcastSessionStillDownOnLoginUp) broadcast(config.broadcastSessionMinecraftStillDown);
			}
			else broadcast(config.broadcastAuthserverMojangDown);
		} else if(!online) {
			authserverMojangDowntimer++;
			if(authserverMojangDowntimer >= config.remainsDownInterval) {
				broadcast(config.broadcastAuthserverMojangStillDown);
				authserverMojangDowntimer = 0;
			}
		}
		authserverMojang = online;
	}
	
	@EventHandler
	public void onPing(ProxyPingEvent ev)
	{
		String modt = ev.getResponse().getMotd();
		
		if(!sessionMinecraft && !authserverMojang) {
			modt = parseModt(config.sessionsAndLoginDown, modt);
		} else if(!sessionMinecraft) {
			modt = parseModt(config.sessionsDown, modt);
		} else if(!authserverMojang) {
			modt = parseModt(config.loginDown, modt);
		}
		
		if(!skinsMinecraft && !minecraftNet) {
			modt = parseModt(config.skinsAndMinecraftNetDown, modt);
		} else if(!skinsMinecraft) {
			modt = parseModt(config.skinsDown, modt);
		} else if(!minecraftNet) {
			modt = parseModt(config.minecraftNetDown, modt);
		}
		
		if(modt != ev.getResponse().getMotd()) {
			ServerPing sp = new ServerPing(
					ev.getResponse().getProtocolVersion(),
					ev.getResponse().getGameVersion(),
					modt,
					ev.getResponse().getCurrentPlayers(),
					ev.getResponse().getMaxPlayers());
			ev.setResponse(sp);
		}
	}
	
	public String parseModt(String s, String motd)
	{
		return ChatColor.translateAlternateColorCodes('&', s.replace("%motd%", motd));
	}
	
	public void broadcast(String s)
	{
		if(!s.isEmpty()) {
			for(String m : ChatColor.translateAlternateColorCodes('&', s).split("\n")) BungeeCord.getInstance().broadcast(m);
		}
	}
}
