package de.rene_zeidler.mojangstatus;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;

public class StatusChecker implements Runnable {
	String json;
	protected Gson gson;
	protected ArrayList<HashMap<String, String>> jsonObject;
	
	MainConfig config;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void run() {
		//References the config for easier accessing
		config = MojangStatus.getInstance().getConfig();
		try {
			//Retrieve status info
			URL url = new URL("http://status.mojang.com/check");
			
			Scanner s = new Scanner(url.openStream());
			this.json = s.next();
			s.close();
			
			//Parse JSON data
			this.gson = new Gson();
			
			Type listType = new StatusChecker.ListType(this).getType();
			this.jsonObject = ((ArrayList) this.gson.fromJson(this.json, listType));
			if(config.debug) MojangStatus.getInstance().getLogger().log(Level.FINE, "[Mojang Status] Successfully downloaded JSON");
		} catch (IOException ex) {
			MojangStatus.getInstance().getLogger().log(Level.FINE, "Error when trying to retrieve Mojang server status data.");
			if(config.debug) ex.printStackTrace();
		}
		try {
			//Expected format is:
			//	[{"minecraft.net":"green"},{"login.minecraft.net":"green"},{"session.minecraft.net":"green"},{"account.mojang.com":"green"},{"auth.mojang.com":"green"},{"skins.minecraft.net":"green"},{"authserver.mojang.com":"green"}]
			//Sets the service status to offline when the corresponding jsonObject has the value "red"
			MojangStatus.getInstance().setMinecraftNetStatus(!((String) ((HashMap) this.jsonObject.get(0)).get("minecraft.net")).equalsIgnoreCase("red"));
			MojangStatus.getInstance().setLoginMinecraftStatus(!((String) ((HashMap) this.jsonObject.get(1)).get("login.minecraft.net")).equalsIgnoreCase("red"));
			MojangStatus.getInstance().setSessionMinecraftStatus(!((String) ((HashMap) this.jsonObject.get(2)).get("session.minecraft.net")).equalsIgnoreCase("red"));
			MojangStatus.getInstance().setAccountMojangStatus(!((String) ((HashMap) this.jsonObject.get(3)).get("account.mojang.com")).equalsIgnoreCase("red"));
			MojangStatus.getInstance().setAuthMojangStatus(!((String) ((HashMap) this.jsonObject.get(4)).get("auth.mojang.com")).equalsIgnoreCase("red"));
			MojangStatus.getInstance().setSkinsMinecraftStatus(!((String) ((HashMap) this.jsonObject.get(5)).get("skins.minecraft.net")).equalsIgnoreCase("red"));
			MojangStatus.getInstance().setAuthServerMojangStatus(!((String) ((HashMap) this.jsonObject.get(6)).get("authserver.mojang.com")).equalsIgnoreCase("red"));
		} catch (NullPointerException ex) {
			//JSON data have an unexpected format, just ignore it
			MojangStatus.getInstance().getLogger().log(Level.FINE, "The downloaded JSON data are invalid or empty");
			if(config.debug) ex.printStackTrace();
		}
	}
	
	class ListType extends TypeToken<ArrayList<HashMap<String, String>>> {
		ListType(StatusChecker paramStatusChecker) {}
	}
}