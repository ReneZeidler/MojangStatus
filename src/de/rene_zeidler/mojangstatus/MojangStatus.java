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
	/**
	 * Current status of the service minecraft.net
	 */
	public static boolean minecraftNet = true;
	/**
	 * Current status of the service login.minecraft.net
	 */
	public static boolean loginMinecraft = true;
	/**
	 * Current status of the service session.minecraft.net
	 */
	public static boolean sessionMinecraft = true;
	/**
	 * Current status of the service account.mojang.com
	 */
	public static boolean accountMojang = true;
	/**
	 * Current status of the service auth.mojang.com
	 */
	public static boolean authMojang = true;
	/**
	 * Current status of the service skins.minecraft.net
	 */
	public static boolean skinsMinecraft = true;
	/**
	 * Current status of the service authserver.mojang.com
	 */
	public static boolean authserverMojang = true;
	public int minecraftNetDowntimer = 0;
	public int loginMinecraftDowntimer = 0;
	public int sessionMinecraftDowntimer = 0;
	public int accountMojangDowntimer = 0;
	public int authMojangDowntimer = 0;
	public int skinsMinecraftDowntimer = 0;
	public int authserverMojangDowntimer = 0;
	/**
	 * Static instance of the plugin itself
	 * Can be accessed with getInstance() 
	 */
	private static MojangStatus instance = null;
	private MainConfig config;
	private ScheduledTask task;
	
	/**
	 * Returns the instance of the plugin
	 * @return instance of MojangStatus
	 */
	public static MojangStatus getInstance()
	{
		if(instance == null) instance = (MojangStatus) BungeeCord.getInstance().getPluginManager().getPlugin("MojangStatus");
		return instance;
	}
	
	/**
	 * Returns the configuration object
	 * @return instance of MainConfig
	 */
	public MainConfig getConfig()
	{
		return config;
	}
	
	public void onEnable()
	{
		this.config = new MainConfig(this);
		
		//register Listener/Commands
		BungeeCord.getInstance().getPluginManager().registerListener(this, this);
		BungeeCord.getInstance().getPluginManager().registerCommand(this, new CommandMCStatus());
		BungeeCord.getInstance().getPluginManager().registerCommand(this, new CommandSetStatus());
		
		//schedule task
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
		
		//cancel task
		task.cancel();
	}
	
	/**
	 * Sets the status of the service minecraft.net
	 * @param online true for online, false for offline
	 */
	public void setMinecraftNetStatus(boolean online)
	{
		if(minecraftNet != online) {	//status has changed
			getLogger().log(Level.INFO, "Minecraft.net just went " + (online ? "online" : "offline"));
			if(online) {	//status changed to online
				minecraftNetDowntimer = 0;	//set downtime timer to 0
				broadcast(config.broadcastMinecraftNetUp);
			}
			else broadcast(config.broadcastMinecraftNetDown);
		} else if(!online) {	//status remains offline
			minecraftNetDowntimer++;	//increment timer
			if(minecraftNetDowntimer >= config.remainsDownInterval) {	//timer exceeded limit
				broadcast(config.broadcastMinecraftNetStillDown);
				minecraftNetDowntimer = 0;	//reset timer
			}
		}
		minecraftNet = online;	//set attribute to new status
	}
	
	/**
	 * Sets the status of the service login.minecraft.net
	 * @param online true for online, false for offline
	 */
	public void setLoginMinecraftStatus(boolean online)
	{
		if(loginMinecraft != online) {	//status has changed
			getLogger().log(Level.INFO, "Login servers (legacy) just went " + (online ? "online" : "offline"));
			if(online) {	//status changed to online
				loginMinecraftDowntimer = 0;	//set downtime timer to 0
				broadcast(config.broadcastLoginMinecraftUp);
			}
			else broadcast(config.broadcastLoginMinecraftDown);
		} else if(!online) {	//status remains offline
			loginMinecraftDowntimer++;	//increment timer
			if(loginMinecraftDowntimer >= config.remainsDownInterval) {	//timer exceeded limit
				broadcast(config.broadcastLoginMinecraftStillDown);
				loginMinecraftDowntimer = 0;	//reset timer
			}
		}
		loginMinecraft = online;	//set attribute to new status
	}
	
	/**
	 * Sets the status of the service session.minecraft.net
	 * @param online true for online, false for offline
	 */
	public void setSessionMinecraftStatus(boolean online)
	{
		if(sessionMinecraft != online) {	//status has changed
			getLogger().log(Level.INFO, "Session servers (legacy) just went " + (online ? "online" : "offline"));
			if(online) {	//status changed to online
				sessionMinecraftDowntimer = 0;	//set downtime timer to 0
				broadcast(config.broadcastSessionMinecraftUp);
				//special broadcast when login servers are still down
				if(!authserverMojang && config.broadcastLoginStillDownOnSessionUp) broadcast(config.broadcastAuthserverMojangStillDown);
			}
			else broadcast(config.broadcastSessionMinecraftDown);
		} else if(!online) {	//status remains offline
			sessionMinecraftDowntimer++;	//increment timer
			if(sessionMinecraftDowntimer >= config.remainsDownInterval) {	//timer exceeded limit
				broadcast(config.broadcastSessionMinecraftStillDown);
				sessionMinecraftDowntimer = 0;	//reset timer
			}
		} 
		sessionMinecraft = online;	//set attribute to new status
	}
	
	/**
	 * Sets the status of the service account.mojang.com
	 * @param online true for online, false for offline
	 */
	public void setAccountMojangStatus(boolean online)
	{
		if(accountMojang != online) {	//status has changed
			getLogger().log(Level.INFO, "Mojang accounts website just went " + (online ? "online" : "offline"));
			if(online) {	//status changed to online
				accountMojangDowntimer = 0;	//set downtime timer to 0
				broadcast(config.broadcastAccountMojangUp);
			}
			else broadcast(config.broadcastAccountMojangDown);
		} else if(!online) {	//status remains offline
			accountMojangDowntimer++;	//increment timer
			if(accountMojangDowntimer >= config.remainsDownInterval) {	//timer exceeded limit
				broadcast(config.broadcastAccountMojangStillDown);
				accountMojangDowntimer = 0;	//reset timer
			}
		}
		accountMojang = online;	//set attribute to new status
	}
	
	/**
	 * Sets the status of the service auth.mojang.com
	 * @param online true for online, false for offline
	 */
	public void setAuthMojangStatus(boolean online)
	{
		if(authMojang != online) {	//status has changed
			getLogger().log(Level.INFO, "Mojang accounts login (legacy) just went " + (online ? "online" : "offline"));
			if(online) {	//status changed to online
				authMojangDowntimer = 0;	//set downtime timer to 0
				broadcast(config.broadcastAuthMojangUp);
			}
			else broadcast(config.broadcastAuthMojangDown);
		} else if(!online) {	//status remains offline
			authMojangDowntimer++;	//increment timer
			if(authMojangDowntimer >= config.remainsDownInterval) {	//timer exceeded limit
				broadcast(config.broadcastAuthMojangStillDown);
				authMojangDowntimer = 0;	//reset timer
			}
		} 
		authMojang = online;	//set attribute to new status
	}
	
	/**
	 * Sets the status of the service skins.minecraft.net
	 * @param online true for online, false for offline
	 */
	public void setSkinsMinecraftStatus(boolean online)
	{
		if(skinsMinecraft != online) {	//status has changed
			getLogger().log(Level.INFO, "Skin servers just went " + (online ? "online" : "offline"));
			if(online) {	//status changed to online
				skinsMinecraftDowntimer = 0;	//set downtime timer to 0
				broadcast(config.broadcastSkinsMinecraftUp);
			}
			else broadcast(config.broadcastSkinsMinecraftDown);
		} else if(!online) {	//status remains offline
			skinsMinecraftDowntimer++;	//increment timer
			if(skinsMinecraftDowntimer >= config.remainsDownInterval) {	//timer exceeded limit
				broadcast(config.broadcastSkinsMinecraftStillDown);
				skinsMinecraftDowntimer = 0;	//reset timer
			}
		}
		skinsMinecraft = online;	//set attribute to new status
	}
	
	/**
	 * Sets the status of the service authserver.mojang.com
	 * @param online true for online, false for offline
	 */
	public void setAuthServerMojangStatus(boolean online)
	{
		if(authserverMojang != online) {	//status has changed
			getLogger().log(Level.INFO, "Authentification service (Minecraft login) just went " + (online ? "online" : "offline"));
			if(online) {	//status changed to online
				authserverMojangDowntimer = 0;	//set downtime timer to 0
				broadcast(config.broadcastAuthserverMojangUp);
				//special broadcast when session servers are still down
				if(!sessionMinecraft && config.broadcastSessionStillDownOnLoginUp) broadcast(config.broadcastSessionMinecraftStillDown);
			}
			else broadcast(config.broadcastAuthserverMojangDown);
		} else if(!online) {	//status remains offline
			authserverMojangDowntimer++;	//increment timer
			if(authserverMojangDowntimer >= config.remainsDownInterval) {	//timer exceeded limit
				broadcast(config.broadcastAuthserverMojangStillDown);
				authserverMojangDowntimer = 0;	//reset timer
			}
		}
		authserverMojang = online;	//set attribute to new status
	}
	
	@EventHandler
	public void onPing(ProxyPingEvent ev)
	{
		//store original MOTD
		String modt = ev.getResponse().getMotd();
		
		if(!sessionMinecraft && !authserverMojang) {	//session + login offline
			modt = parseModt(config.sessionsAndLoginDown, modt);
		} else if(!sessionMinecraft) {	//only session offline
			modt = parseModt(config.sessionsDown, modt);
		} else if(!authserverMojang) {	//only login offline
			modt = parseModt(config.loginDown, modt);
		}
		
		if(!skinsMinecraft && !minecraftNet) {			//skins and minecraft.net offline
			modt = parseModt(config.skinsAndMinecraftNetDown, modt);
		} else if(!skinsMinecraft) {	//only skins offline
			modt = parseModt(config.skinsDown, modt);
		} else if(!minecraftNet) {		//only minecraft.net offline
			modt = parseModt(config.minecraftNetDown, modt);
		}
		
		if(modt != ev.getResponse().getMotd()) {	//MOTD was changed
			//create new ServerPing with changed MOTD
			ServerPing sp = new ServerPing(
					ev.getResponse().getProtocolVersion(),
					ev.getResponse().getGameVersion(),
					modt,
					ev.getResponse().getCurrentPlayers(),
					ev.getResponse().getMaxPlayers());
			ev.setResponse(sp);	//replace ServerPing in the event
		}
	}
	
	/**
	 * Parses an MOTD string
	 * Replaces %modt% with the old MODT and translates color codes with &
	 * @param s Original String
	 * @param motd Old MOTD
	 * @return Parsed string
	 */
	public String parseModt(String s, String motd)
	{
		return ChatColor.translateAlternateColorCodes('&', s.replace("%motd%", motd));
	}
	
	/**
	 * Broadcasts the message if it is not empty
	 * The message gets split into multiple messages when there are line breaks with \n
	 * @param s message
	 */
	public void broadcast(String s)
	{
		if(!s.isEmpty()) for(String m : ChatColor.translateAlternateColorCodes('&', s).split("\n")) BungeeCord.getInstance().broadcast(m);
	}
}
