package de.rene_zeidler.mojangstatus;

import java.util.EnumMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import net.craftminecraft.bungee.bungeeyaml.bukkitapi.InvalidConfigurationException;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.event.EventHandler;

public class MojangStatus extends Plugin implements Listener {
	
	public static enum Service {
		MINECRAFTNET,
		LOGINMINECRAFT,
		SESSIONMINECRAFT,
		ACCOUNTMOJANG,
		AUTHMOJANG,
		SKINSMINECRAFT,
		AUTHSERVERMOJANG
	}
	
	public static enum BroadcastType {
		UP,
		DOWN,
		STILLDOWN
	}
	
	/**
	 * Current status of a service (true -> online, false -> offline)
	 */
	private EnumMap<Service, Boolean> currentStatus = new EnumMap<Service, Boolean>(Service.class);
	/**
	 * The status of a service that was last broadcasted
	 */
	private EnumMap<Service, Boolean> lastBroadcastedStatus = new EnumMap<Service, Boolean>(Service.class);
	/**
	 * How long the status of a service hasn't changed
	 */
	private EnumMap<Service, Integer> timerUnchanged = new EnumMap<Service, Integer>(Service.class);

	
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
			getLogger().log(Level.WARNING, "Error while saving the config:");
			e.printStackTrace();
		}
		
		//cancel task
		task.cancel();
	}
	
	/**
	 * Returns the status of the given service that was last broadcasted
	 * @param service
	 * @return
	 */
	public boolean getLastBroadcastedStatus(Service service)
	{
		return lastBroadcastedStatus.containsKey(service) ? lastBroadcastedStatus.get(service) : true;
	}
	
	/**
	 * Sets the status of a service and displays broadcasts if needed
	 * @param service
	 * @param online
	 */
	public void setLastBroadcastedStatus(Service service, boolean newStatus)
	{
		lastBroadcastedStatus.put(service, newStatus);
	}
	
	/**
	 * Returns the time that the status of a service hasn't changed
	 * @param service
	 * @return
	 */
	public int getUnchangedTime(Service service)
	{
		return timerUnchanged.containsKey(service) ? timerUnchanged.get(service) : 0;
	}
	
	/**
	 * Sets the time that the status of a service hasn't changed
	 * @param service
	 * @param online
	 */
	public void setUnchangedTime(Service service, int newTime)
	{
		timerUnchanged.put(service, newTime);
	}
	
	/**
	 * Returns the current status of the given service
	 * @param service
	 * @return
	 */
	public boolean getStatus(Service service)
	{
		return currentStatus.containsKey(service) ? currentStatus.get(service) : true;
	}
	
	/**
	 * Sets the status of a service and displays broadcasts if needed
	 * @param service
	 * @param online
	 */
	public void setStatus(Service service, boolean newStatus)
	{
		boolean oldStatus = getStatus(service);
		currentStatus.put(service, newStatus);
		
		if(oldStatus == newStatus) {
			//status is unchanged
			setUnchangedTime(service, getUnchangedTime(service) + 1); //increment timer by 1
			if(!newStatus && getUnchangedTime(service) % config.remainsDownInterval == 0) //service is down and the remainsDownInterval has elapsed
				broadcast(service, BroadcastType.STILLDOWN);
		} else {
			//status has changed
			setUnchangedTime(service, 0); //reset timer
			getLogger().log(Level.INFO, service.toString() + " just went " + (newStatus ? "online" : "offline"));
		}
		
		//  is online &&  wasn't broadcasted before         && buffer time has elapsed
		if( newStatus && !getLastBroadcastedStatus(service) && getUnchangedTime(service) == config.broadcastUpWait  ) {
			broadcast(service, BroadcastType.UP  );
			//Special broadcast when only one of the services for login + session goes up but the other is down
			if(service == Service.AUTHSERVERMOJANG && !getStatus(Service.SESSIONMINECRAFT)) broadcast(Service.SESSIONMINECRAFT, BroadcastType.STILLDOWN);
			if(service == Service.SESSIONMINECRAFT && !getStatus(Service.AUTHSERVERMOJANG)) broadcast(Service.AUTHSERVERMOJANG, BroadcastType.STILLDOWN);
		}
		// is offline &&  wasn't broadcasted before         && buffer time has elapsed
		if(!newStatus &&  getLastBroadcastedStatus(service) && getUnchangedTime(service) == config.broadcastDownWait)
			broadcast(service, BroadcastType.DOWN);
	}
	
	/**
	 * Gets the broadcast string for a service and type from the config
	 * @param service
	 * @param type
	 * @return
	 */
	public String getBroadcast(Service service, BroadcastType type)
	{
		if(type == BroadcastType.UP) {
			switch (service) {
				case ACCOUNTMOJANG:    return config.broadcastAccountMojangUp;
				case AUTHMOJANG:       return config.broadcastAuthMojangUp;
				case AUTHSERVERMOJANG: return config.broadcastAuthserverMojangUp;
				case LOGINMINECRAFT:   return config.broadcastLoginMinecraftUp;
				case MINECRAFTNET:     return config.broadcastMinecraftNetUp;
				case SESSIONMINECRAFT: return config.broadcastSessionMinecraftUp;
				case SKINSMINECRAFT:   return config.broadcastSkinsMinecraftUp;
			}
		} else if(type == BroadcastType.DOWN) {
			switch (service) {
				case ACCOUNTMOJANG:    return config.broadcastAccountMojangDown;
				case AUTHMOJANG:       return config.broadcastAuthMojangDown;
				case AUTHSERVERMOJANG: return config.broadcastAuthserverMojangDown;
				case LOGINMINECRAFT:   return config.broadcastLoginMinecraftDown;
				case MINECRAFTNET:     return config.broadcastMinecraftNetDown;
				case SESSIONMINECRAFT: return config.broadcastSessionMinecraftDown;
				case SKINSMINECRAFT:   return config.broadcastSkinsMinecraftDown;
			}
		} else if(type == BroadcastType.STILLDOWN) {
			switch (service) {
				case ACCOUNTMOJANG:    return config.broadcastAccountMojangStillDown;
				case AUTHMOJANG:       return config.broadcastAuthMojangStillDown;
				case AUTHSERVERMOJANG: return config.broadcastAuthserverMojangStillDown;
				case LOGINMINECRAFT:   return config.broadcastLoginMinecraftStillDown;
				case MINECRAFTNET:     return config.broadcastMinecraftNetStillDown;
				case SESSIONMINECRAFT: return config.broadcastSessionMinecraftStillDown;
				case SKINSMINECRAFT:   return config.broadcastSkinsMinecraftStillDown;
			}
		}
		
		return "";
	}
	
	/**
	 * Broadcasts the message for a service if specified for the type
	 * @param service
	 * @param type Which broadcast to show (server went up, down or is still down)
	 */
	public void broadcast(Service service, BroadcastType type)
	{
		setLastBroadcastedStatus(service, (type == BroadcastType.UP));
		broadcast(getBroadcast(service, type));
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
	
	@EventHandler
	public void onPing(ProxyPingEvent ev)
	{
		//store original MOTD
		String motd = ev.getResponse().getDescription();
		
		if(!getStatus(Service.SESSIONMINECRAFT) && !getStatus(Service.AUTHSERVERMOJANG)) { //session + login offline
			motd = parseModt(config.sessionsAndLoginDown, motd);
		} else if(!getStatus(Service.SESSIONMINECRAFT)) { //only session offline
			motd = parseModt(config.sessionsDown, motd);
		} else if(!getStatus(Service.AUTHSERVERMOJANG)) { //only login offline
			motd = parseModt(config.loginDown, motd);
		}
		
		if(!getStatus(Service.SKINSMINECRAFT) && !getStatus(Service.MINECRAFTNET)) { //skins and minecraft.net offline
			motd = parseModt(config.skinsAndMinecraftNetDown, motd);
		} else if(!getStatus(Service.SKINSMINECRAFT)) { //only skins offline
			motd = parseModt(config.skinsDown, motd);
		} else if(!getStatus(Service.MINECRAFTNET)) { //only minecraft.net offline
			motd = parseModt(config.minecraftNetDown, motd);
		}
		
		if(motd != ev.getResponse().getDescription()) //MOTD was changed
			ev.getResponse().setDescription(motd);
	}
	
	/**
	 * Parses an MOTD string
	 * Replaces %motd% with the old MODT, translates color codes with & and converts \n into new lines
	 * @param s Original String
	 * @param motd Old MOTD
	 * @return Parsed string
	 */
	public String parseModt(String s, String motd)
	{
		return ChatColor.translateAlternateColorCodes('&', s.replace("\\n", "\n").replace("%motd%", motd));
	}

}
